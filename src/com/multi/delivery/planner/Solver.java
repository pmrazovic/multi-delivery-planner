package com.multi.delivery.planner;

/**
 * Created by pero on 05/12/2016.
 */
public class Solver {
    TestInstance testInstance;
    int[][] currentSolutionPaths;
    float currentSolutionLength;

    public Solver(TestInstance testInstance) {
        this.testInstance = testInstance;
        // Creating empty solution
        this.currentSolutionPaths = new int[testInstance.delivererCount][];
        for (int i = 0; i < testInstance.delivererCount; i++) {
            this.currentSolutionPaths[i] = new int[testInstance.deliverers[i].length];
        }
    }

    public void solve() {
        generateInitialSolution();
        localSearch();
        perturbate();
    }
}
