package com.multi.delivery.planner;

/**
 * Created by pero on 05/12/2016.
 */
public class TestInstance {
    // Total number of nodes
    int nodeCount;
    // Total number of deliverers
    int delivererCount;
    // Original node IDs
    int[] nodeIDs;
    // Edge costs
    float[][] edgeCosts;
    // Deliverers' set of nodes
    int[][] deliverers;
    // Solution paths
    int[][] solutions;
    // Score of the best known solution
    float optimalScore;
    // Score of the best know MTSP solution
    float optimalMTSPScore;
    // Size of the solution pool used to find optimal solution
    int poolSize;
    // Computation time
    float computationTime;

    public TestInstance(int nodeCount, int delivererCount, int[] nodeIDs, float[][] edgeCosts, int[][] deliverers,
                        int[][] solutions, float optimalScore, float optimalMTSPScore, int poolSize, float computationTime) {
        this.nodeCount = nodeCount;
        this.delivererCount = delivererCount;
        this.nodeIDs = nodeIDs;
        this.edgeCosts = edgeCosts;
        this.deliverers = deliverers;
        this.solutions = solutions;
        this.optimalScore = optimalScore;
        this.optimalMTSPScore = optimalMTSPScore;
        this.poolSize = poolSize;
        this.computationTime = computationTime;
    }
}
