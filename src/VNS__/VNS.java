package VNS__;

import java.nio.file.Path;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Vector;

public class VNS {
	// Event class (represents arrivals/departures at/from node)
	public class Event implements Comparable<Event> {
		int routeID;
		int nodeRouteIdx;
		String type;
		LocalTime time;

		public Event(int routeID, int nodeRouteIdx, String type, LocalTime time) {
			this.routeID = routeID;
			this.nodeRouteIdx = nodeRouteIdx;
			this.type = type;
			this.time = time;
		}

		@Override
		public int compareTo(Event other) {
			return time.compareTo(other.time);
		}
	}

	TestInstance testInstance;
	Solution currentSol;

	int maxIter = 100, kMax = 10, rclSize;
	Random random = new Random();
	double perturbRatio, greedinessRatio;
	int totalNodes;
	PriorityQueue<RouteNode> tempRcl;
	// Because perturbation changes the solution, I do not keep the
	// neighborhoods, instead compute them in every iteration.
	// ArrayList<Neighborhood> neighborhoods = new ArrayList<Neighborhood>();

	// it is for the distance from the best edge coming to the node
	HashMap<Integer, HashMap<Integer, Double>> editedInvertedCosts;

	public VNS(TestInstance testInstance, double greedinessRatio, double perturbRatio) {
		this.greedinessRatio = greedinessRatio;
		this.perturbRatio = perturbRatio;
		init(testInstance);
	}

	public VNS(Path path, double greedinessRatio, double perturbRatio) {

	}

	public VNS(Path path, int maxIter, int kMax, double greedinessRatio, double perturbRatio) {
		this.greedinessRatio = greedinessRatio;
		this.perturbRatio = perturbRatio;
		this.maxIter = maxIter;
		this.kMax = kMax;
		init(TestInstanceParser.parse(path));
	}

	// current solution is computed via GRASP, kept in solver
	private void init(TestInstance testInstance) {
		this.testInstance = testInstance;
		currentSol = new Solution(testInstance.routes,
				getTotalDurationOf(testInstance.routes, new PriorityQueue<RouteNode>()));
		// to see the initial solution result
		System.out.println("init " + currentSol);

		this.testInstance.routes = constructiveGrasp(this.testInstance.routes);
		totalNodes = 0;
		for (LinkedList<Integer> route : testInstance.routes) {
			totalNodes += route.size();
		}

		rclSize = (int) Math.round(totalNodes * greedinessRatio);
		tempRcl = new PriorityQueue<RouteNode>(rclSize);
		// @getTotalDurationOf method computes the total duration and puts the
		// largest waiting times to the given priority queue
		// I will then assign the pq for the solution
		currentSol = new Solution(testInstance.routes, getTotalDurationOf(testInstance.routes, tempRcl));
		currentSol.restrictedWaitList = tempRcl;

		int bestScore;
		
		//this can be performed in test instance parser 
		//in the parser, I only keep the to-from hashmap as inverted costs
		//in the editedInvertedCosts I keep the relative distance from the best edge cost 
		editedInvertedCosts = new HashMap<Integer, HashMap<Integer, Double>>();
		for (int to : testInstance.invertedEdgeCosts.keySet()) {
			bestScore = Integer.MAX_VALUE;
			for (int from : testInstance.invertedEdgeCosts.get(to).keySet()) {
				if (bestScore > testInstance.invertedEdgeCosts.get(to).get(from)) {
					bestScore = testInstance.invertedEdgeCosts.get(to).get(from);
				}

			}
			HashMap<Integer, Double> tempMap = new HashMap<Integer, Double>();

			for (int from : testInstance.invertedEdgeCosts.get(to).keySet()) {
				// distance from the best according to percentage

				// putting the mnus value of it, because of the ease in treemap
				tempMap.put(from, testInstance.invertedEdgeCosts.get(to).get(from) - bestScore / (double) bestScore);
			}
			editedInvertedCosts.put(to, tempMap);

		}

	}

	public void runVNS() {
		int iter = 0, k = 1;
		Solution perturbed;
		ArrayList<LinkedList<Integer>> routes;
		Solution candidate;
		k = 1;

		Solution bestSol = currentSol;

		// while loop can be designed differently, e.g. not finish before all
		// iteration number is handled
		while (iter < maxIter) {
			while (k <= kMax) {
				System.out.println("k " + k);
				// apply perturbation
				routes = perturbation(currentSol.routes);
				tempRcl = new PriorityQueue<RouteNode>(rclSize);
				
				perturbed = new Solution(routes, getTotalDurationOf(routes, tempRcl));
				perturbed.restrictedWaitList = tempRcl;

				// best candidate based on perturbed solution
				candidate = localSearchBest(perturbed, k);

				if (candidate.score < currentSol.score) {
					// renew the solution
					currentSol = candidate;
					k = 1;
				} else {
					k++;
				}
			}
			if (currentSol.score < bestSol.score) {
				bestSol = currentSol;
			}

			this.testInstance.routes = constructiveGrasp(this.testInstance.routes);
			tempRcl = new PriorityQueue<RouteNode>(rclSize);
			currentSol = new Solution(testInstance.routes, getTotalDurationOf(testInstance.routes, tempRcl));
			System.out.println(iter++ + " iter");
			k = 1;

		}

		System.out.println(bestSol);

	}

	// updates given array, by inserting nodeIndex element to ith element
	public void insertInplace(LinkedList<Integer> route, int i, int nodeindex) {
		route.add(i, route.remove(nodeindex));
	}

	// returns the best soluiton in the given neighborhood
	private Solution localSearchBest(Solution perturbed, int k) {
		Neighborhood neighK = getNeighborhood(perturbed, k, k);
		return neighK.getSolutions().stream().max((s1, s2) -> s1.score - s2.score).get();
	}

	

	// move application is only for one selected node TODO
	private ArrayList<Solution> getNeighborhoodElems(Solution sol, boolean weightOrWait) {
		ArrayList<LinkedList<Integer>> routes = sol.routes;
		ArrayList<Solution> sols = new ArrayList<>();

		int maxNum = (int) Math.round((totalNodes * greedinessRatio));
		PriorityQueue<RouteNode> rcl = new PriorityQueue<RouteNode>(maxNum);

		double tempRatio;
		for (int z = 0; z < routes.size(); z++) {
			for (int i = 1; i < routes.get(z).size(); i++) {
				if (weightOrWait || sol.restrictedWaitList.size() == 0) {
					// weight is selected the travel cost
					tempRatio = editedInvertedCosts.get(routes.get(z).get(i)).get(routes.get(z).get(i - 1));
					if (rcl.size() < maxNum) {
						// if not full, put anyway
						rcl.add(new RouteNode(z, i, tempRatio));
					} else if (tempRatio > rcl.peek().wait) {
						// if it is less than the last, then omit the last one
						// and
						// put this
						rcl.remove();
						rcl.add(new RouteNode(z, i, tempRatio));
					}
				} else {
					rcl = sol.restrictedWaitList;
				}
			}
		}

		RouteNode selectedNode = new Vector<>(rcl).elementAt(random.nextInt(rcl.size()));

		for (int j = 1; j < routes.get(selectedNode.routeId).size(); j++) {
			if (selectedNode.nodeId != j) {
				ArrayList<LinkedList<Integer>> altered = new ArrayList<LinkedList<Integer>>(routes.size());

				// copy routes
				for (int l = 0; l < routes.size(); l++) {
					altered.add((LinkedList<Integer>) routes.get(l).clone());
				}

				// insert move
				insertInplace(altered.get(selectedNode.routeId), j, selectedNode.nodeId);

				// add solution
				tempRcl = new PriorityQueue<RouteNode>(rclSize);
				Solution _sol = new Solution(altered, getTotalDurationOf(altered, tempRcl));
				_sol.restrictedWaitList = tempRcl;
				sols.add(_sol);

			}
		}

		return sols;
	}

	

	// modified -- choose only one node
	private Solution getBestElemsOfNeighborhood(Solution sol, boolean weightOrwait) {
		Solution best = new Solution(null, Integer.MAX_VALUE);
		ArrayList<LinkedList<Integer>> altered = new ArrayList<LinkedList<Integer>>(sol.routes.size());

		// copy routes
		for (int l = 0; l < sol.routes.size(); l++) {
			altered.add((LinkedList<Integer>) sol.routes.get(l).clone());
		}

		for (int z = 0; z < sol.routes.size(); z++) {
			for (int i = 1; i < sol.routes.get(z).size(); i++) {
				for (int j = 1; j < sol.routes.get(z).size(); j++) {
					if (i != j) {
						// insert move
						insertInplace(altered.get(z), i, j);
						tempRcl = new PriorityQueue<RouteNode>();
						int score = getTotalDurationOf(altered, tempRcl);

						// renew best
						if (score < best.score) {
							best = new Solution(altered, score);
							best.restrictedWaitList = tempRcl;
						}

						// convert to the original
						altered.set(z, (LinkedList<Integer>) sol.routes.get(z).clone());
					}
				}
			}
		}
		return best;
	}

	private String stringOf(Solution sol) {
		ArrayList<LinkedList<Integer>> routes = sol.routes;
		String text = "";
		for (LinkedList<Integer> route : routes) {
			for (int vertex : route)
				text = text.concat(vertex + "\t");
			text = text.concat("\n");
		}
		return text;
	}

	// returns the kth neighborhood of th given routes
	// recursively computes the neighborhoods
	private Neighborhood getNeighborhood(Solution perturbed, int k) {
		Neighborhood neighborhood = new Neighborhood();

		if (k == 1) {
			neighborhood.set(getNeighborhoodElems(perturbed, true));
		} else {
			Neighborhood prev = getNeighborhood(perturbed, k - 1);
			// apply local search for each element of the previous neighborhood
			// set
			boolean weightOrwait = (k < 5) ? true : false;
			for (Solution prevSol : prev.getSolutions()) {
				neighborhood.addAll(getNeighborhoodElems(prevSol, weightOrwait));
			}
		}
		return neighborhood;
	}

	// it is a variation of the previous method, gets rid of keeping all
	// elements of the final neighborhood (only the best of the neighborhood
	// will be useful), instead keeps the best.
	// PS not to change the other methods, I put it in a neighborhood and return
	// it.
	private Neighborhood getNeighborhood(Solution perturbed, int k, int finalK) {
		Neighborhood neighborhood = new Neighborhood();
		if (k == 1) {
			neighborhood.set(getNeighborhoodElems(perturbed, true));
		} else {
			Neighborhood prev = getNeighborhood(perturbed, k - 1, finalK);
			// apply local search for each element of the previous neighborhood
			// set
			boolean weightOrwait = (k < 6) ? true : false;
			if (k != finalK)
				for (Solution prevSol : prev.getSolutions()) {
					neighborhood.addAll(getNeighborhoodElems(prevSol, weightOrwait));
				}
			else {
				// if this, use the other method
				Solution best = new Solution(null, Integer.MAX_VALUE);
				Solution cur;
				for (Solution prevSol : prev.getSolutions()) {
					cur = getBestElemsOfNeighborhood(prevSol, weightOrwait);
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
	public ArrayList<LinkedList<Integer>> perturbation(ArrayList<LinkedList<Integer>> routes) {
		ArrayList<LinkedList<Integer>> perturbed = new ArrayList<LinkedList<Integer>>(routes.size());
		// clone each
		for (int i = 0; i < routes.size(); i++) {
			perturbed.add((LinkedList<Integer>) routes.get(i).clone());
		}
		int perturbAmount, initialIndex;
		for (LinkedList<Integer> route : perturbed) {
			// exclude initial, not allows exceeding capacities
			perturbAmount = (int) Math.ceil(perturbRatio * (route.size() - 1));
			// at least two perturbation
			perturbAmount = (perturbAmount == 1) ? 2 : perturbAmount;
			// perturb place(perturbRatio percent of sequential nodes)
			initialIndex = 1 + random.nextInt(route.size() - 1 - perturbAmount);
			// it maybe whole random, not based on a sequential one
			for (int i = initialIndex; i < initialIndex + perturbAmount; i++) {
				swap(route, i, initialIndex + random.nextInt(perturbAmount));
			}
		}
		return perturbed;
	}

	private ArrayList<LinkedList<Integer>> constructiveGrasp(ArrayList<LinkedList<Integer>> routes) {
		ArrayList<LinkedList<Integer>> nRoutes = new ArrayList<LinkedList<Integer>>(routes.size());
		for (int i = 0; i < routes.size(); i++) {
			nRoutes.add(constructiveGrasp(routes.get(i)));
		}
		return nRoutes;
	}

	public LinkedList<Integer> constructiveGrasp(LinkedList<Integer> route) {
		int maxNum = (int) Math.round(greedinessRatio * route.size());
		return constructiveGrasp(route, maxNum);
	}

	public LinkedList<Integer> constructiveGrasp(LinkedList<Integer> route, int maxNum) {
		// not to change the current, it can be used again
		LinkedList<Integer> newRoute = (LinkedList<Integer>) route.clone();
		if (maxNum < 1)
			return newRoute;// or maybe shuffled
		if (maxNum >= route.size()) {
			shuffle(newRoute);
			return newRoute;
		}
		int cursor = 1, currentNode = newRoute.getFirst(), randIndex;
		// keeping the indices
		ArrayList<Integer> rcl = new ArrayList<Integer>(maxNum);
		// the last one is in the correct place
		while (cursor < newRoute.size() - 1) {
			// no need to rcl, it is random for the rest of the array
			if (newRoute.size() - cursor < maxNum) {
				randIndex = cursor + random.nextInt(newRoute.size() - cursor);
				// swap(newRoute, cursor, randIndex);
				newRoute.add(cursor, newRoute.remove(randIndex));
			} else {
				currentNode = newRoute.get(cursor - 1);
				rcl.add(cursor);
				// scan the rest of the array, if rcl is full, and a candidate
				// is closer to current node, omit the last one and put new one
				// as sorted
				for (int i = cursor + 1; i < newRoute.size(); i++) {
					if (rcl.size() < maxNum) {
						// for keeping the max valued index as the last
						putAsSorted(rcl, currentNode, newRoute, i);
					} else {
						if (testInstance.edgeCosts.get(currentNode).get(newRoute.get(i)) < testInstance.edgeCosts
								.get(currentNode).get(newRoute.get(rcl.get(rcl.size() - 1)))) {
							rcl.remove(rcl.size() - 1);
							putAsSorted(rcl, currentNode, newRoute, i);
						}
					}
				}

				// swap the element in cursor from the rcl
				int rand = rcl.get(random.nextInt(rcl.size()));
				newRoute.add(cursor, newRoute.remove(rand));

				rcl.clear();
			}

			cursor++;

		}
		return newRoute;
	}

	// helper method
	public void putAsSorted(ArrayList<Integer> list, int currentNode, LinkedList<Integer> route, int elemIndex) {

		if (list.size() == 0)
			list.add(elemIndex);
		else {
			int i = 0;
			int elem = route.get(elemIndex);
			while (i < list.size() && testInstance.edgeCosts.get(currentNode)
					.get(route.get(list.get(i))) < testInstance.edgeCosts.get(currentNode).get(elem)) {
				i++;
			}
			list.add(i, elemIndex);
		}
	}

	private void shuffle(LinkedList<Integer> arr) {
		// cannot change the first index
		for (int i = arr.size() - 1; i > 1; i--) {
			swap(arr, i, random.nextInt(i) + 1);
		}
	}

	private void swap(LinkedList<Integer> route, int i, int j) {
		int temp = route.get(i);
		route.set(i, route.get(j));
		route.set(j, temp);
	}

	public int getTotalDurationOf(ArrayList<LinkedList<Integer>> currentSolutionRoutes, PriorityQueue<RouteNode> rcl) {
		int totalTravel = 0, totalWait = 0;
		// Initialize the event queue with arrivals at the first node of each
		// route
		// Sorted event queue
		PriorityQueue<Event> eventQueue = new PriorityQueue<Event>();
		for (int i = 0; i < currentSolutionRoutes.size(); i++) {
			Event newEvent = new Event(i, 0, "ARRIVAL", this.testInstance.routeStarts[i]);
			eventQueue.add(newEvent);
		}

		// Node queue contains departure times of both parked and waiting
		// vehicles
		HashMap<Integer, ArrayList<LocalTime>> nodeQueues = new HashMap<>();

		// We process events (arrivals and departures) until the event queue is
		// not empty
		while (eventQueue.size() > 0) {
			Event currentEvent = eventQueue.poll();

			// Create node queue for the current node if one does not exist
			int currentNodeID = currentSolutionRoutes.get(currentEvent.routeID).get(currentEvent.nodeRouteIdx);
			if (!nodeQueues.containsKey(currentNodeID)) {
				// All of the nodes are empty at the beginning (no parked
				// vehicles, no waiting line)
				nodeQueues.put(currentNodeID, new ArrayList<>());
			}

			// Processing arrival and departure events until event queue is not
			// empty
			if (currentEvent.type == "ARRIVAL") {
				// For arrival events we compute departure events using nodes'
				// waiting queues and duration of the visits

				LocalTime newEventTime = null;
				if (nodeQueues.get(currentNodeID).size() < this.testInstance.nodeCapacities.get(currentNodeID)) {
					// Node is not full
					newEventTime = currentEvent.time.plusSeconds(
							this.testInstance.deliveryDurations.get(currentEvent.routeID).get(currentNodeID));
				} else {
					// Node is full and waiting line is not empty
					int waitingInLine = nodeQueues.get(currentNodeID).size()
							- this.testInstance.nodeCapacities.get(currentNodeID);
					LocalTime waitUntil = nodeQueues.get(currentNodeID).get(waitingInLine);
					newEventTime = waitUntil.plusSeconds(
							this.testInstance.deliveryDurations.get(currentEvent.routeID).get(currentNodeID));
					totalWait += ChronoUnit.SECONDS.between(currentEvent.time, waitUntil);
					add(rcl, new RouteNode(currentEvent.routeID, currentEvent.nodeRouteIdx,
							(int) ChronoUnit.SECONDS.between(currentEvent.time, waitUntil)));// TODO
																								// check

				}
				// Adding new departure time to the node's queue
				nodeQueues.get(currentNodeID).add(newEventTime);
				Collections.sort(nodeQueues.get(currentNodeID));
				// Adding new departure event to the event queue
				Event newEvent = new Event(currentEvent.routeID, currentEvent.nodeRouteIdx, "DEPARTURE", newEventTime);
				eventQueue.add(newEvent);

			} else if (currentEvent.type == "DEPARTURE") {
				// For departure events we compute arrival events at the next
				// node by adding travel time to the current time point

				nodeQueues.get(currentNodeID).remove(currentEvent.time);
				// If the current node is not the last in the route, create new
				// arrival event
				if (currentSolutionRoutes.get(currentEvent.routeID).size() > currentEvent.nodeRouteIdx + 1) {
					int travelTime = this.testInstance.edgeCosts.get(currentNodeID)
							.get(currentSolutionRoutes.get(currentEvent.routeID).get(currentEvent.nodeRouteIdx + 1));
					LocalTime newEventTime = currentEvent.time.plusSeconds(travelTime);
					Event newEvent = new Event(currentEvent.routeID, currentEvent.nodeRouteIdx + 1, "ARRIVAL",
							newEventTime);
					eventQueue.add(newEvent);
					totalTravel += travelTime;
				}
			}
		}

		return totalTravel + totalWait + totalDeliveryDuration();
	}

	private void add(PriorityQueue<RouteNode> rcl, RouteNode routeNode) {
		rcl.add(routeNode);
		if (rcl.size() > rclSize)
			rcl.remove();// removes the initial
	}

	private int totalDeliveryDuration(int routeID) {
		int total = 0;

		for (int vertex : testInstance.routes.get(routeID))
			total += testInstance.deliveryDurations.get(routeID).get(vertex);
		return total;
	}

	private int totalDeliveryDuration() {
		int total = 0;
		for (int r = 0; r < testInstance.routeCount; r++)
			total += totalDeliveryDuration(r);
		return total;
	}

}
