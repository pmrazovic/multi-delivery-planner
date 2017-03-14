package com.multi.delivery.planner;

import java.time.LocalTime;
import java.util.ArrayList;
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

	HashMap<Integer, HashMap<Integer, Double>> editedInvertedCosts;

	// Routes' set of nodes
	ArrayList<ArrayList<Integer>> routes;
	// Stop durations
	HashMap<Integer, HashMap<Integer, Integer>> deliveryDurations;
	// Routes' start times
	LocalTime[] routeStarts;
	// TSP ordering
	ArrayList<ArrayList<Integer>> tspRoutes;
	// Complexity of test instance (Jaccard distance)
	float complexity;

	public TestInstance(int nodeCount, int routeCount, int[] nodeIDs, HashMap<Integer, Integer> nodeCapacities,
			HashMap<Integer, HashMap<Integer, Integer>> edgeCosts,
			HashMap<Integer, HashMap<Integer, Double>> editedInvertedCosts, ArrayList<ArrayList<Integer>> routes,
			HashMap<Integer, HashMap<Integer, Integer>> deliveryDurations, LocalTime[] routeStarts,
			ArrayList<ArrayList<Integer>> tspRoutes, float complexity) {

		this.nodeCount = nodeCount;
		this.routeCount = routeCount;
		this.nodeIDs = nodeIDs;
		this.nodeCapacities = nodeCapacities;
		this.edgeCosts = edgeCosts;
		this.editedInvertedCosts = editedInvertedCosts;
		this.routes = routes;
		this.deliveryDurations = deliveryDurations;
		this.routeStarts = routeStarts;
		this.tspRoutes = tspRoutes;
		this.complexity = complexity;

	}
}
