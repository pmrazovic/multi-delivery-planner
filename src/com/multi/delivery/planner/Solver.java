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
        int[][] routes = new int[this.testInstance.routeCount][];
        for (int i = 0; i < this.testInstance.routeCount; i++) {
            routes[i] = createRandomRoute(this.testInstance.routes[i], graspGreedinessRate);
        }
        return new Solution(this.testInstance, routes);
    }

    // Creates random route from a given visit list
    private int[] createRandomRoute(int visitList[], float graspGreedinessRate) {
        // Initial route
        int[] route = visitList.clone();

        int cursor = 0;
        while (cursor < route.length - 1) {
            // Restricted candidate list as a treemap (hashmap with sorted keys)
            // Keys are distances, and values are node indices in current route
            int currentNodeIdx = cursor;
            TreeMap<Integer, Integer> rcl = new TreeMap<>();
            int maxRcl = (int) Math.ceil(graspGreedinessRate * (route.length - cursor - 1));

            // Populating RCL
            for (int i = cursor+1; i < route.length; i++) {
                rcl.put(this.testInstance.edgeCosts.get(route[cursor]).get(route[i]), i);
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
            swap(route, cursor+1, randomNodeIdx);

            // Increasing cursor
            cursor += 1;
        }

        return route;
    }

    // Perturbs given solution, i.e. perturbationRate percentage of solution is shuffled
    private Solution perturbateSolution(Solution oldSolution, float perturbationRate) {
        int[][] perturbedRoutes = new int[oldSolution.routes.length][];
        for (int i = 0; i < perturbedRoutes.length; i++) {
            // Cloning original routes
            perturbedRoutes[i] = oldSolution.routes[i].clone();
            // Size of the perturbation segment
            int perturbationLength = (int) Math.ceil((perturbedRoutes[i].length - 1) * perturbationRate);
            // If perturbation segment consist of only one node, we increase it by one
            perturbationLength = perturbationLength == 1 ? 2 : perturbationLength;
            // Start position of the perturbation segment
            int pertubationStartIdx = 1 + randomGenerator.nextInt(perturbedRoutes[i].length - perturbationLength);
            // Shuffle the perturbation segment
            for (int j = pertubationStartIdx; j < pertubationStartIdx + perturbationLength; j++) {
                swap(perturbedRoutes[i], j, pertubationStartIdx + randomGenerator.nextInt(perturbationLength));
            }
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

    // Helper methods ----------------------------------------------------------

    // Swaps two node in given route
    private void swap(int[] route, int i, int j) {
        int temp = route[i];
        route[i] = route[j];
        route[j] = temp;
    }



}
