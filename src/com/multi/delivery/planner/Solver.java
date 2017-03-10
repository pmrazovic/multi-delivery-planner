package com.multi.delivery.planner;

import java.util.*;
import java.util.stream.Collectors;
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
    }

    // Starts solving process
    public Solution run() {
        int kMax = 20;
        int countNoImprovement = 0;
        // Create random solution using GRASP
        Solution bestSolution = createRandomSolution();
        // Repeat until solution has not been improved in certain number of itteration
        while (countNoImprovement < 300) {
            // Create random solution using GRASP
            Solution initialSolution = createRandomSolution();
            // Improve the solution using VNS
            Solution improvedSolution = vns(initialSolution,kMax);
            if (improvedSolution.totalCost < bestSolution.totalCost) {
                // Solution is improved
                bestSolution = improvedSolution;
                countNoImprovement = 0;
            } else {
                // Solution is not improved
                countNoImprovement++;
            }
        }
        return bestSolution;
    }

    // Method implements VNS metaheuristic
    public Solution vns(Solution initialSolution, int kMax) {
        int k = 1;
        Solution bestSolution = initialSolution;
        while (k <= kMax) {
            // TODO: Tune the perturbationRate parameter if needed
            float perturbationRate = 0.7f/kMax;
            // Perturb the solution
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
        // TODO: Tune the graspGreedinessRate parameter if needed
        // graspGreedinessRate is used to compute size of RCL
        float graspGreedinessRate = randomGenerator.nextFloat();
        ArrayList<ArrayList<Integer>> routes = new ArrayList<>();
        // Call GRASP construction method for each route
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

    // Perturbs given solution, i.e. perturbationRate percentage of routes are shuffled
    private Solution perturbateSolution(Solution oldSolution, float perturbationRate) {
        // Total number of routes that will be shuffled
        int perturbationCount = (int) Math.ceil(oldSolution.testInstance.routeCount * perturbationRate);
        // Choose routes at random
        ArrayList<Integer> perturbatedRouteIdxs = uniqueRandomFromRange(0,oldSolution.testInstance.routeCount,perturbationCount);

        // Copy old routes
        ArrayList<ArrayList<Integer>> perturbedRoutes = new ArrayList<>(oldSolution.routes);
        for (int routeIdx : perturbatedRouteIdxs) {
            // Cloning original route
            ArrayList<Integer> newPerturbedRoute = new ArrayList<>(oldSolution.routes.get(routeIdx));
            // Size of the perturbation segment
            // For now we shuffle all of the nodes in the route, except the first one
            // Other option is to shuffle only a percentage of nodes, i.e. (int) Math.ceil((newPerturbedRoute.size() - 1) * shufflePercenatge)
            int perturbationLength = newPerturbedRoute.size() - 1;
            // If perturbation segment consist of only one node, we increase it by one
            perturbationLength = perturbationLength == 1 ? 2 : perturbationLength;
            // Start position of the perturbation segment
            int pertubationStartIdx = 1 + randomGenerator.nextInt(newPerturbedRoute.size() - perturbationLength);
            for (int j = pertubationStartIdx; j < pertubationStartIdx + perturbationLength; j++) {
                Collections.swap(newPerturbedRoute, j, pertubationStartIdx + randomGenerator.nextInt(perturbationLength));
            }
            perturbedRoutes.set(routeIdx, newPerturbedRoute);
        }

        return new Solution(oldSolution.testInstance, perturbedRoutes);
    }

    // Searches a solution neighbourhood for a local optimum
    // The method combines two local search moves:
    //  (1) shift move - repositions a node within a route to reduce waiting time
    //  (2) 2opt move - 2opt swap within route to reduce travel time
    private Solution localSearch(Solution perturbedSolution, int k) {
        Solution bestSolution = perturbedSolution;

        // Local search looks for the best possible shift and 2opt move until the improvement cannot be found
        boolean improved = true;
        while (improved) {
            improved = false;
            // TODO: Tune the greedinesRate parameter if needed
            float greedinessRate = randomGenerator.nextFloat();

            // ----- 1. Shift moves to reduce waiting time -----
            Solution candidateSolution = shiftMove(bestSolution,greedinessRate,k);
            // ----- 2. 2opt moves to reduce travel time -----
            candidateSolution = twoOptMove(candidateSolution,greedinessRate,k);

            // Check if solution is improved
            if (candidateSolution.totalCost < bestSolution.totalCost) {
                bestSolution = candidateSolution;
                improved = true;
            }
        }

        return bestSolution;
    }

    // Chooses k nodes at random from RCL, and searches for the best shift move
    private Solution shiftMove(Solution bestSolution, float greedinessRate, int k) {
        Solution candidateSolution = bestSolution;
        // Get ranked waitings
        ArrayList<Solution.Waiting> waitings = bestSolution.nodeWaitings;
        if (waitings.size() != 0) {
            // Restrict the candidate list
            int maxRcl = (int) Math.ceil(greedinessRate * waitings.size());
            // If maxRcl is too small, or k is to large
            if (maxRcl < k && k <= waitings.size()) {
                maxRcl = k;
            } else if (k > waitings.size()) {
                maxRcl = k = waitings.size();
            }
            // Choose waitings at random
            ArrayList<Integer> randomWaitingIdxs = uniqueRandomFromRange(0,maxRcl,k);

            for (Integer randomWaitingIdx: randomWaitingIdxs) {
                // Choose one waiting at random from the RCL
                Solution.Waiting randomWaiting = waitings.get(randomWaitingIdx);
                // Target route that will be changed
                ArrayList<Integer> targetRoute = bestSolution.routes.get(randomWaiting.routeIdx);
                // We will move node in targetRoute from position randomWaiting.nodeRouteIdx to a position
                // which will most increase the total score
                for (int movePosition = 1; movePosition < targetRoute.size(); movePosition++) {
                    ArrayList<Integer> updatedTargetRoute = moveNodeWithinRoute(targetRoute, randomWaiting.nodeRouteIdx, movePosition);
                    ArrayList<ArrayList<Integer>> updatedRoutes = new ArrayList<>(bestSolution.routes);
                    updatedRoutes.set(randomWaiting.routeIdx, updatedTargetRoute);
                    Solution newSolution = new Solution(this.testInstance, updatedRoutes);
                    if (newSolution.totalCost < candidateSolution.totalCost) {
                        candidateSolution = newSolution;
                    }
                }
            }
        }
        return candidateSolution;
    }

    // Chooses k routes at random from RCL, and searches for the best 2opt move
    private Solution twoOptMove(Solution bestSolution, float greedinessRate, int k) {
        Solution candidateSolution = bestSolution;
        // Restrict the candidate list
        int maxRcl = (int) Math.ceil(greedinessRate * this.testInstance.routeCount);
        // If maxRcl is too small, or k is to large
        if (maxRcl < k && k <= this.testInstance.routeCount) {
            maxRcl = k;
        } else if (k > this.testInstance.routeCount) {
            maxRcl = k = this.testInstance.routeCount;
        }
        // RCL is composed of maxRcl longest routes (with highest travel time)
        // We first randomly choose indexes of elements in RCL
        ArrayList<Integer> randomRclIdxs = uniqueRandomFromRange(0,maxRcl,k);

        for (int randomRclIdx : randomRclIdxs) {
            // Target route that will be updated
            ArrayList<Integer> targetRoute = bestSolution.routes.get(bestSolution.routesByTravelCost[randomRclIdx]);
            // We will perform 2opt move in targetRoute at the position which will most increase the total score
            for (int i = 1; i < targetRoute.size()-1; i++) {
                for (int j = i+1; j < targetRoute.size(); j++) {
                    ArrayList<Integer> updatedTargetRoute = twoOptSwap(targetRoute,i,j);
                    ArrayList<ArrayList<Integer>> updatedRoutes = new ArrayList<>(bestSolution.routes);
                    updatedRoutes.set(bestSolution.routesByTravelCost[randomRclIdx], updatedTargetRoute);
                    Solution newSolution = new Solution(this.testInstance, updatedRoutes);
                    if (newSolution.totalCost < candidateSolution.totalCost) {
                        candidateSolution = newSolution;
                    }
                }
            }
        }
        return candidateSolution;
    }

    // Helper methods -----------------------------------------------------

    // Method creates new array list by moving an element from oldIdx to newIdx
    private ArrayList<Integer> moveNodeWithinRoute(ArrayList<Integer> originalRoute, int oldIdx, int newIdx) {
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

    // Method creates new array list by performing 2opt swap
    private ArrayList<Integer> twoOptSwap(ArrayList<Integer> originalRoute, int i, int j) {
        ArrayList<Integer> newRoute = new ArrayList<>(originalRoute);
        int l = j;
        int limit = i + ((j-i)/2);
        for (int k = i; k <= limit; k++) {
            int temp = newRoute.get(k);
            newRoute.set(k,newRoute.get(l));
            newRoute.set(l,temp);
            l--;
        }
        return newRoute;
    }

    // Method returns n random numbers from range [from,to]
    private  ArrayList<Integer> uniqueRandomFromRange(int from, int to, int count) {
        ArrayList<Integer> indexes = new ArrayList<>();
        for (int i = from; i < to; i++) {
            indexes.add(i);
        }
        Collections.shuffle(indexes);
        return new ArrayList<Integer>(indexes.subList(0, count));
    }

}
