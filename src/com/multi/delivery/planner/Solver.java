package com.multi.delivery.planner;

import java.util.*;

/**
 * Created by pero on 05/12/2016.
 */
public class Solver {
    // Random generator
    Random randomGenerator = new Random();
    // Test instance that needs to be solved
    TestInstance testInstance;

    public Solver(TestInstance testInstance) {
        // Assigning test instance
        this.testInstance = testInstance;
        Solution solution = new Solution(testInstance);
        solution.computeTimeCosts();
    }

    // Starts solving process
    public Solution run() {
        Solution solution = new Solution(this.testInstance);
        Solution newSolution = createRandomSolution();
        vns();
        return newSolution;
    }

    // VNS procedure
    public Solution vns() {
        int k = 1;
        int kMax = 5;

        Solution bestSolution = createRandomSolution();
        while (k <= kMax) {
            // TODO: Perturbation rate can be chosen w.r.t. current depth k
            float perturbationRate = 0.2f;
            Solution perturbedSolution = perturbateSolution(bestSolution, perturbationRate);

            // Best candidate solution based on perturbed solution
            Solution candidateSolution = localSearch(perturbedSolution,k);

        }

        return bestSolution;

    }

    // Creates random solution using GRASP
    private Solution createRandomSolution() {
        // TODO: Tune the grasp greedines rate
        float graspGreedinessRate = randomGenerator.nextFloat();
        ArrayList<ArrayList<Integer>> routes = new ArrayList<>();
        for (int i = 0; i < this.testInstance.routeCount; i++) {
            routes.add(createRandomRoute(this.testInstance.routes.get(i), graspGreedinessRate));
        }
        return new Solution(this.testInstance, routes);
    }

    // Creates random route from a given visit list
    private ArrayList<Integer> createRandomRoute(ArrayList<Integer> visitList, float graspGreedinessRate) {
        // Initial route
        ArrayList<Integer> route = new ArrayList<>(visitList);

        int cursor = 0;
        while (cursor < route.size() - 1) {
            // Restricted candidate list as a treemap (hashmap with sorted keys)
            // Keys are distances, and values are node indices in current route
            int currentNodeIdx = cursor;
            TreeMap<Integer, Integer> rcl = new TreeMap<>();
            int maxRcl = (int) Math.ceil(graspGreedinessRate * (route.size() - cursor - 1));

            // Populating RCL
            for (int i = cursor+1; i < route.size(); i++) {
                rcl.put(this.testInstance.edgeCosts.get(route.get(cursor)).get(route.get(i)), i);
                // If size of RCL is larger than maxRCl we remove the farthest node in the list
                if (rcl.size() > maxRcl) {
                    rcl.remove(rcl.lastKey());
                }
            }

            // Choosing one node from RCL as random
            ArrayList<Integer> keys = new ArrayList<>(rcl.keySet());
            int randomKey = keys.get(randomGenerator.nextInt(keys.size()));
            int randomNodeIdx = rcl.get(randomKey);

            // Swapping nodes in route
            Collections.swap(route, cursor+1, randomNodeIdx);

            // Increasing cursor
            cursor += 1;
        }

        return route;
    }

    // Perturbs given solution, i.e. perturbationRate percentage of solution is shuffled
    private Solution perturbateSolution(Solution oldSolution, float perturbationRate) {
        ArrayList<ArrayList<Integer>> perturbedRoutes = new ArrayList<>();
        for (int i = 0; i < oldSolution.routes.size(); i++) {
            // Cloning original routes
            ArrayList<Integer> newPerturbedRoute = new ArrayList<>(oldSolution.routes.get(i));
            // Size of the perturbation segment
            int perturbationLength = (int) Math.ceil((newPerturbedRoute.size() - 1) * perturbationRate);
            // If perturbation segment consist of only one node, we increase it by one
            perturbationLength = perturbationLength == 1 ? 2 : perturbationLength;
            // Start position of the perturbation segment
            int pertubationStartIdx = 1 + randomGenerator.nextInt(newPerturbedRoute.size() - perturbationLength);
            // Shuffle the perturbation segment
            for (int j = pertubationStartIdx; j < pertubationStartIdx + perturbationLength; j++) {
                Collections.swap(newPerturbedRoute, j, pertubationStartIdx + randomGenerator.nextInt(perturbationLength));
            }
            perturbedRoutes.add(newPerturbedRoute);
        }

        return new Solution(this.testInstance, perturbedRoutes);
    }

    // Searches a solution neighbourhood for a local optimum
    private Solution localSearch(Solution perturbedSolution, int k) {
        for (int i = 0; i < k; i++) {
            ArrayList<Solution.Waiting> waitings = perturbedSolution.nodeWaitings;
            // TODO: Finish the implementation
        }

        throw new UnsupportedOperationException("Not implemented yet...");
    }

}
