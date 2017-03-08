package VNS__;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class Solution {
	ArrayList<LinkedList<Integer>> routes;
	PriorityQueue<RouteNode> restrictedWaitList;
	int score;

	public Solution(ArrayList<LinkedList<Integer>> routes, int score) {

		super();
		this.routes = routes;
		this.score = score;
	}

	public Solution(ArrayList<LinkedList<Integer>> routes, int score, PriorityQueue<RouteNode> rcl) {
		// it means the wait nodes will be kept
		super();
		this.routes = routes;
		this.score = score;
		this.restrictedWaitList = rcl;
	}

	@Override
	public String toString() {
		String text = "Score " + score + "\n";
		for (LinkedList<Integer> route : routes) {
			for (int vertex : route)
				text = text.concat(vertex + "\t");
			text = text.concat("\n");
		}
		return text;
	}
}
