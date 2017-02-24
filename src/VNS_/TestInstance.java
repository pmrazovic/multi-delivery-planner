package VNS_;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by pero on 05/12/2016.
 */
public class TestInstance {
	// Total number of nodes
	int nodeCount;
	// Total number of routes
	int routeCount;
	// Original node IDs
	int[] nodeIDs;
	// Nodes' capacities
	HashMap<Integer, Integer> nodeCapacities;

	// Edge costs
	HashMap<Integer, HashMap<Integer, Integer>> edgeCosts;
	// Routes' set of nodes
	int[][] routes;
	// Stop durations
	HashMap<Integer, HashMap<Integer, Integer>> deliveryDurations;
	// Routes' start times
	LocalTime[] routeStarts;

	public TestInstance(int nodeCount, int routeCount, int[] nodeIDs, HashMap<Integer, Integer> nodeCapacities,
			HashMap<Integer, HashMap<Integer, Integer>> edgeCosts, int[][] routes,
			HashMap<Integer, HashMap<Integer, Integer>> deliveryDurations, LocalTime[] routeStarts) {
		this.nodeCount = nodeCount;
		this.routeCount = routeCount;
		this.nodeIDs = nodeIDs;
		this.nodeCapacities = nodeCapacities;
		this.edgeCosts = edgeCosts;
		this.routes = routes;
		this.deliveryDurations = deliveryDurations;
		this.routeStarts = routeStarts;
	}
}
