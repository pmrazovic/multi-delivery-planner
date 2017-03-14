package com.multi.delivery.planner;

public class RouteNode implements Comparable<RouteNode> {
	int routeId, nodeId;
	double wait;

	public RouteNode(int routeId, int nodeId, double wait) {
		super();
		this.routeId = routeId;
		this.nodeId = nodeId;
		this.wait = wait;
	}

	@Override
	public int compareTo(RouteNode o) {
		if (this.wait > o.wait)
			return 1;
		else if (this.wait < o.wait)
			return -1;
		else
			return 0;
	}

}
