package com.multi.delivery.planner;

import java.util.Random;

/**
 * Created by pero on 05/12/2016.
 */
public class Solver {
    private static Random random = new Random();

    // Test instance that needs to be solved
    TestInstance testInstance;
    // Current solution paths
    int[][] currentSolutionPaths;
    // Total cost of the current solution
    float totalCurrentLength;
    // Cost of the each path in the current solution
    float[] currentPathLengths;
    // Visit intervals for each node and deliverer in current solution path
    // e.g. currentVisitIntervals[deliverer][position_in_path] = [visit_start, visit_end]
    float[][][] currentVisitIntervals;
    // Paths of the best solution found
    int[][] bestSolutionPaths;
    // Cost of the best solution found
    float totalBestLength;

    public Solver(TestInstance testInstance) {
        // Assigning test instance
        this.testInstance = testInstance;
        // Creating empty solution
        this.currentSolutionPaths = new int[this.testInstance.delivererCount][];
        this.currentVisitIntervals = new float[this.testInstance.delivererCount][][];
        for (int i = 0; i < testInstance.delivererCount; i++) {
            this.currentSolutionPaths[i] = new int[testInstance.deliverers[i].length];
            this.currentVisitIntervals[i] = new float[testInstance.deliverers[i].length][2];
        }
        this.totalCurrentLength = 0;
        this.currentPathLengths = new float[this.testInstance.delivererCount];
    }

    // Method solves assigned test instance
    public void solve() {
        // TODO: These 3 procedures needs to be developed, and integrated into some metaheuristic framework
        generateInitialSolution();
        localSearch();
        perturbate(1.0f);
    }

    // Private methods ------------------------------------------------------------------

    // Method generates initial feasible (valid) solution from empty or partial solution
    private void generateInitialSolution() {
        // TODO: Implement heuristic procedure for building feasible (valid) initial solutions
        this.currentSolutionPaths = this.testInstance.deliverers;
        computeCurrentCostsAndVisitIntervals();
    }

    // Method intensifies search process, i.e. searches for local optima, by applying heuristic search moves
    private void localSearch() {
        // TODO: To be implemented...
    }

    // Method diversifies search process, i.e. escapes local optima by randomly shuffling parts of the current solution paths
    // perturbationRatio defines percentage of nodes that will be shuffled in each solution path
    // Complexity of the method is O(n^2)
    private void perturbate(float perturbationRatio) {
        for (int i = 0; i < this.currentSolutionPaths.length; i++) {
            int sizeOfShuffle = (int) Math.floor(perturbationRatio * this.currentSolutionPaths[i].length);
            int positionOfShuffle = random.nextInt(this.currentSolutionPaths[i].length-sizeOfShuffle+1);
            // Part of path [positionOfShuffle, positionOfShuffle + sizeOfShuffle - 1] will be shuffled
            this.currentSolutionPaths[i] = shuffle(this.currentSolutionPaths[i],positionOfShuffle,sizeOfShuffle);
        }
        computeCurrentCostsAndVisitIntervals();
    }

    // Method computes individual and total cost of paths in the current solution, and visit intervals in each path
    // Complexity of the method is O(n^2)
    private void computeCurrentCostsAndVisitIntervals() {
        this.totalCurrentLength = 0;
        // For each path in current solution
        for (int i = 0; i < this.currentSolutionPaths.length; i++) {
            this.currentPathLengths[i] = 0;
            // For each node in current path
            for (int j = 0; j < this.currentSolutionPaths[i].length-1; j++) {
                // Arrival at node j
                this.currentVisitIntervals[i][j][0] = this.currentPathLengths[i];
                // Departure from node j
                this.currentVisitIntervals[i][j][1] = this.currentVisitIntervals[i][j][0] + this.testInstance.nodeCosts[this.currentSolutionPaths[i][j]];
                // Increase cost of path i
                this.currentPathLengths[i] += this.testInstance.nodeCosts[this.currentSolutionPaths[i][j]] +
                        this.testInstance.edgeCosts[this.currentSolutionPaths[i][j]][this.currentSolutionPaths[i][j+1]];
            }
            // Adding costs for the last node in path i
            // Arrival at last node
            this.currentVisitIntervals[i][this.currentSolutionPaths[i].length-1][0] = this.currentPathLengths[i];
            // Departure from last node
            this.currentVisitIntervals[i][this.currentSolutionPaths[i].length-1][1] = this.currentVisitIntervals[i][this.currentSolutionPaths[i].length-1][0] +
                    this.testInstance.nodeCosts[this.currentSolutionPaths[i][this.currentSolutionPaths[i].length-1]];
            // Increase cost of path i
            this.currentPathLengths[i] += this.testInstance.nodeCosts[this.currentSolutionPaths[i][this.currentSolutionPaths[i].length-1]];

            this.totalCurrentLength += this.currentPathLengths[i];
        }
    }

    // Method shuffles part of path specified with start position and size (number of elements)
    // Complexity of the method is O(n)
    private int[] shuffle(int[] path, int start, int size) {
        for (int i = start; i < start + size; i++) {
            int randomPosition = random.nextInt(size-1) + start;
            int temp = path[i];
            path[i] = path[randomPosition];
            path[randomPosition] = temp;
        }
        return path;
    }
}
