package com.multi.delivery.planner;

import java.util.*;
import java.util.stream.IntStream;

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
        int kMax = 5;
        int countNoImprovement = 0;
        Solution bestSolution = createRandomSolution();
        while (countNoImprovement < 1000) {
            Solution initialSolution = createRandomSolution();
            Solution improvedSolution = vns(initialSolution,kMax);
            if (improvedSolution.totalCost < bestSolution.totalCost) {
                bestSolution = improvedSolution;
                countNoImprovement = 0;
            } else {
                countNoImprovement++;
            }
        }
        return bestSolution;
    }

    // VNS procedure
    public Solution vns(Solution initialSolution, int kMax) {
        int k = 1;
        Solution bestSolution = initialSolution;
        while (k <= kMax) {
            // TODO: Tune the perturbation rate
            float perturbationRate = 1.0f/kMax;
            Solution perturbedSolution = perturbateSolution(bestSolution, perturbationRate);
            // Best candidate solution based on perturbed solution
            Solution candidateSolution = localSearch(perturbedSolution,k);

            if (candidateSolution.totalCost < bestSolution.totalCost) {
                bestSolution = candidateSolution;
                k = 1;
            } else {
                k = k + 1;
            }
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
        Solution bestSolution = perturbedSolution;
        boolean improved = true;
        while (improved) {
            improved = false;
            // TODO: Tune the greedines rate
            float greedinessRate = randomGenerator.nextFloat();
            // Get ranked waitings
            ArrayList<Solution.Waiting> waitings = bestSolution.nodeWaitings;

            // TODO: If there is no waiting we choose a node to be moved in another way
            if (waitings.size() == 0) break;

            // Restrict the candidate list
            int maxRcl = (int) Math.ceil(greedinessRate * waitings.size());
            // Choose one waiting at random from the RCL
            Solution.Waiting randomWaiting = waitings.get(randomGenerator.nextInt(maxRcl));
            // Target route that will be changed
            ArrayList<Integer> targetRoute = perturbedSolution.routes.get(randomWaiting.routeIdx);
            // We will move node in targetRoute from position randomWaiting.nodeRouteIdx to a position
            // which will most increase the total score
            for (int movePosition = 1; movePosition < targetRoute.size(); movePosition++) {
                ArrayList<Integer> updatedTargetRoute = moveNodeWithingRoute(targetRoute,randomWaiting.nodeRouteIdx,movePosition);
                ArrayList<ArrayList<Integer>> updatedRoutes = new ArrayList<>(perturbedSolution.routes);
                updatedRoutes.set(randomWaiting.routeIdx, updatedTargetRoute);
                Solution newSolution = new Solution(this.testInstance,updatedRoutes);
                if (newSolution.totalCost < bestSolution.totalCost) {
                    bestSolution = newSolution;
                    improved = true;
                }
            }
        }

        return bestSolution;
    }

    // Helper methods -----------------------------------------------------

    // Method creates new array list by moving an element from oldIdx to newIdx
    private ArrayList<Integer> moveNodeWithingRoute(ArrayList<Integer> originalRoute, int oldIdx, int newIdx) {
        ArrayList<Integer> newRoute = new ArrayList<>(originalRoute);
        // We could simply use: route.add(newIdx, newRoute.remove(oldIdx)),
        // but the following method is more efficient
        Integer fromValue = newRoute.get(oldIdx);
        int delta = oldIdx < newIdx ? 1 : -1;
        for (int i = oldIdx; i != newIdx; i += delta) {
            newRoute.set(i, newRoute.get(i + delta));
        }
        newRoute.set(newIdx, fromValue);
        return newRoute;
    }


}
