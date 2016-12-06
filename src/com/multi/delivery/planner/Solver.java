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
    // Paths of the best solution found
    int[][] bestSolutionPaths;
    // Cost of the best solution found
    float totalBestLength;

    public Solver(TestInstance testInstance) {
        // Assigning test instance
        this.testInstance = testInstance;
        // Creating empty solution
        this.currentSolutionPaths = new int[testInstance.delivererCount][];
        for (int i = 0; i < testInstance.delivererCount; i++) {
            this.currentSolutionPaths[i] = new int[testInstance.deliverers[i].length];
        }
        this.totalCurrentLength = 0;
        this.currentPathLengths = new float[this.testInstance.delivererCount];
    }

    // Method solves assigned test instance
    public void solve() {
        // TODO: These 3 procedures needs to be developed, and integrated into some metaheuristic framework
        generateInitialSolution();
        localSearch();
        perturbate(0.4f);
    }

    // Private methods ------------------------------------------------------------------

    // Method generates initial feasible (valid) solution from empty or partial solution
    private void generateInitialSolution() {
        // TODO: Implement heuristic procedure for building feasible (valid) initial solutions
        this.currentSolutionPaths = this.testInstance.deliverers;
        computeTotalCurrentLength();
    }

    // Method intensifies search process, i.e. searches for local optima, by applying heuristic search moves
    private void localSearch() {
        // TODO: To be implemented...
    }

    // Method diversifies search process, i.e. escapes local optima by randomly shuffling parts of the current solution paths
    // perturbationRatio defines percentage of nodes that will be shuffled in each solution path
    private void perturbate(float perturbationRatio) {
        for (int i = 0; i < this.currentSolutionPaths.length; i++) {
            int sizeOfShuffle = (int) Math.floor(perturbationRatio * this.currentSolutionPaths[i].length);
            int positionOfShuffle = random.nextInt(this.currentSolutionPaths[i].length-sizeOfShuffle+1);
            // Part of path [positionOfShuffle, positionOfShuffle + sizeOfShuffle - 1] will be shuffled
            this.currentSolutionPaths[i] = shuffle(this.currentSolutionPaths[i],positionOfShuffle,sizeOfShuffle);
        }
        computeTotalCurrentLength();
    }

    // Method computes total cost of the current solution
    private void computeTotalCurrentLength() {
        float length = 0;
        for (int i = 0; i < this.currentSolutionPaths.length; i++) {
            float pathLength = computePathLength(this.currentSolutionPaths[i]);
            length += pathLength;
            this.currentPathLengths[i] = pathLength;
        }
        this.totalCurrentLength = length;
    }

    // Method computes total cost of a single path
    private float computePathLength(int[] path) {
        float length = 0;
        for (int i = 0; i < path.length-1; i++) {
            length += this.testInstance.edgeCosts[i][i+1];
        }
        return length;
    }

    // Method shuffles part of path specified with start position and size (number of elements)
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
