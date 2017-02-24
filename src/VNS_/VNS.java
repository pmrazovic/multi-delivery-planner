package VNS_;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class VNS {

	TestInstance testInstance;
	int currentSolutionScore;

	int maxIter = 100, kMax = 10;
	Random random = new Random();
	private Solver solver;// the routes in solver is the current result
	double perturbRatio, greedinessRatio;
	// Because perturbation changes the solution, I do not keep the
	// neighborhoods, instead compute them in every iteration.
	// ArrayList<Neighborhood> neighborhoods = new ArrayList<Neighborhood>();

	public VNS(TestInstance testInstance, double greedinessRatio, double perturbRatio) {
		this.greedinessRatio = greedinessRatio;
		this.perturbRatio = perturbRatio;
		init(testInstance);
	}

	public VNS(Path path, double greedinessRatio, double perturbRatio) {
		this.greedinessRatio = greedinessRatio;
		this.perturbRatio = perturbRatio;
		init(TestInstanceParser.parse(path));

	}

	public VNS(Path path, int maxIter, int kMax, double greedinessRatio, double perturbRatio) {
		this(path, greedinessRatio, perturbRatio);
		this.maxIter = maxIter;
		this.kMax = kMax;
	}

	public Solver getSolver() {
		return solver;
	}

	// current solution is computed via GRASP, kept in solver
	private void init(TestInstance testInstance) {
		this.testInstance = testInstance;
		this.testInstance.routes = constructiveGrasp(this.testInstance.routes);
		solver = new Solver(this.testInstance);
		currentSolutionScore = solver.allToursCost();
	}

	public void runVNS() {
		int iter = 0, k = 1;
		int[][] perturbed;
		Solution candidate;
		k = 1;
	
		// while loop can be designed differently, e.g. not finish before all
		// iteration number is handled
		while (iter < maxIter && k <= kMax) {
			// apply perturbation
			perturbed = perturbation(solver.getCurrentSolutionRoutes());

			// best candidate based on perturbed solution
			candidate = localSearchBest(perturbed, k);

			if (candidate.score < currentSolutionScore) {
				// renew the solution
				solver.updateCurrentSolutionRoutes(candidate.routes);
				currentSolutionScore = candidate.score;
				k = 1;
			} else {
				k++;
			}
			iter++;
		}

	}

	// updates given array, by inserting nodeIndex element to ith element
	public void insertInplace(int[] route, int i, int nodeindex) {
		int insertElem = route[nodeindex];
		if (i < nodeindex) {
			System.arraycopy(route, i, route, i + 1, nodeindex - i);
		} else {
			System.arraycopy(route, nodeindex + 1, route, nodeindex, i - nodeindex);
		}
		route[i] = insertElem;
	}

	// returns the best soluiton in the given neighborhood
	private Solution localSearchBest(int[][] perturbed, int k) {
		Neighborhood neighK = getNeighborhood(perturbed, k, k);
		return neighK.getSolutions().stream().max((s1, s2) -> s1.score - s2.score).get();
	}

	// 1 move applied solutions (over the given routes)
	private ArrayList<Solution> getNeighborhoodElems(int[][] routes) {
		ArrayList<Solution> sols = new ArrayList<>();

		for (int z = 0; z < routes.length; z++) {
			for (int i = 0; i < routes[z].length; i++) {
				for (int j = 0; j < routes[z].length; j++) {
					if (i != j) {
						int[][] altered = new int[routes.length][];

						// copy routes
						for (int l = 0; l < altered.length; l++) {
							altered[l] = routes[l].clone();
						}

						// insert move
						insertInplace(altered[z], i, j);

						// add solution
						sols.add(new Solution(altered, solver.getTotalDurationOf(altered)));

					}
				}
			}
		}
		return sols;
	}

	// the best element in the 1-move neighborhood
	// similar to @getNeighborhoodElems but returns the best of the solutions.
	private Solution getBestElemsOfNeighborhood(int[][] routes) {
		Solution best = new Solution(null, Integer.MAX_VALUE);
		int[][] altered = new int[routes.length][];

		// copy routes
		for (int l = 0; l < altered.length; l++) {
			altered[l] = routes[l].clone();
		}

		for (int z = 0; z < routes.length; z++) {
			for (int i = 0; i < routes[z].length; i++) {
				for (int j = 0; j < routes[z].length; j++) {
					if (i != j) {
						// insert move
						insertInplace(altered[z], i, j);
						int score = solver.getTotalDurationOf(altered);

						// renew best
						if (score < best.score)
							best = new Solution(altered, score);

						// convert to the original
						altered[z] = routes[z].clone();
					}
				}
			}
		}
		return best;
	}

	private String stringOf(int[][] routes) {
		String text = "";
		for (int[] route : routes) {
			for (int vertex : route)
				text = text.concat(vertex + "\t");
			text = text.concat("\n");
		}
		return text;
	}

	// returns the kth neighborhood of th given routes
	// recursively computes the neighborhoods
	private Neighborhood getNeighborhood(int[][] perturbed, int k) {
		Neighborhood neighborhood = new Neighborhood();

		if (k == 1) {
			neighborhood.set(getNeighborhoodElems(perturbed));
		} else {
			Neighborhood prev = getNeighborhood(perturbed, k - 1);
			// apply local search for each element of the previous neighborhood
			// set
			for (Solution prevSol : prev.getSolutions()) {
				neighborhood.addAll(getNeighborhoodElems(prevSol.routes));
			}
		}
		return neighborhood;
	}

	// it is a variation of the previous method, gets rid of keeping all
	// elements of the final neighborhood (only the best of the neighborhood
	// will be useful), instead keeps the best.
	// PS not to change the other methods, I put it in a neighborhood and return
	// it.
	private Neighborhood getNeighborhood(int[][] perturbed, int k, int finalK) {
		Neighborhood neighborhood = new Neighborhood();
		if (k == 1) {
			neighborhood.set(getNeighborhoodElems(perturbed));
		} else {
			Neighborhood prev = getNeighborhood(perturbed, k - 1, finalK);
			// apply local search for each element of the previous neighborhood
			// set
			if (k != finalK)
				for (Solution prevSol : prev.getSolutions()) {
					neighborhood.addAll(getNeighborhoodElems(prevSol.routes));
				}
			else {
				// if this, use the other method
				Solution best = new Solution(null, Integer.MAX_VALUE);
				Solution cur;
				for (Solution prevSol : prev.getSolutions()) {
					cur = getBestElemsOfNeighborhood(prevSol.routes);
					if (cur.score < best.score)
						best = cur;
				}
				neighborhood.add(best);
			}
		}

		return neighborhood;
	}

	// the method perturbs one portion of the results(in each route)
	// swaps them in the portion, it can be changed
	public int[][] perturbation(int[][] routes) {
		int[][] perturbed = new int[routes.length][];
		// clone each
		for (int i = 0; i < perturbed.length; i++) {
			perturbed[i] = routes[i].clone();
		}
		int perturbAmount, initialIndex;
		for (int[] route : perturbed) {
			// exclude initial, not allows exceeding capacities
			perturbAmount = (int) Math.ceil(perturbRatio * (route.length - 1));
			// perturb place(perturbRatio percent of sequential nodes)
			initialIndex = 1 + random.nextInt(route.length - 1 - perturbAmount);
			// it maybe whole random, not based on a sequential one
			for (int i = initialIndex; i < initialIndex + perturbAmount; i++) {
				swap(route, i, initialIndex + random.nextInt(perturbAmount));
			}
		}
		return perturbed;
	}

	private int[][] constructiveGrasp(int[][] routes) {
		int[][] nRoutes = new int[routes.length][];
		for (int i = 0; i < nRoutes.length; i++) {
			nRoutes[i] = constructiveGrasp(routes[i]);
		}
		return nRoutes;
	}

	public int[] constructiveGrasp(int[] route) {
		int maxNum = (int) Math.round(greedinessRatio * route.length);
		return constructiveGrasp(route, maxNum);
	}

	public int[] constructiveGrasp(int[] route, int maxNum) {
		// not to change the current, it can be used again
		int[] newRoute = route.clone();
		if (maxNum < 1)
			return newRoute;// or maybe shuffled TODO let me check
		if (maxNum >= route.length) {
			shuffle(newRoute);
			return newRoute;
		}
		int cursor = 1, currentNode = newRoute[0], randIndex;
		// keeping the indices
		ArrayList<Integer> rcl = new ArrayList<Integer>(maxNum);
		// the last one is in the correct place
		while (cursor < newRoute.length - 1) {
			// no need to rcl, it is random for the rest of the array
			if (newRoute.length - cursor < maxNum) {
				randIndex = cursor + random.nextInt(newRoute.length - cursor);
				swap(newRoute, cursor, randIndex);
			} else {
				currentNode = newRoute[cursor - 1];
				rcl.add(cursor);
				// scan the rest of the array, if rcl is full, and a candidate
				// is closer to current node, omit the last one and put new one
				// as sorted
				for (int i = cursor + 1; i < newRoute.length; i++) {
					if (rcl.size() < maxNum) {
						// for keeping the max valued index as the last
						putAsSorted(rcl, currentNode, newRoute, i);
					} else {
						if (testInstance.edgeCosts.get(currentNode).get(newRoute[i]) < testInstance.edgeCosts
								.get(currentNode).get(newRoute[rcl.get(rcl.size() - 1)])) {
							rcl.remove(rcl.size() - 1);
							putAsSorted(rcl, currentNode, newRoute, i);
						}
					}
				}

				// swap the element in cursor from the rcl
				swap(newRoute, cursor, rcl.get(random.nextInt(rcl.size())));

				rcl.clear();
			}

			cursor++;

		}
		return newRoute;
	}

	// helper method
	public void putAsSorted(ArrayList<Integer> list, int currentNode, int[] route, int elemIndex) {

		if (list.size() == 0)
			list.add(elemIndex);
		else {
			int i = 0;
			while (i < list.size() && testInstance.edgeCosts.get(currentNode)
					.get(route[list.get(i)]) < testInstance.edgeCosts.get(currentNode).get(route[elemIndex])) {
				i++;
			}
			list.add(i, elemIndex);
		}
	}

	private void shuffle(int[] arr) {
		// cannot change the first index
		for (int i = arr.length - 1; i > 1; i--) {
			swap(arr, i, random.nextInt(i) + 1);
		}
	}

	private void swap(int[] route, int i, int j) {
		int temp = route[i];
		route[i] = route[j];
		route[j] = temp;

	}

}
