package com.multi.delivery.planner;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.PriorityQueue;

/**
 * Created by pero on 05/12/2016.
 */
public class Solver {

    // Event class (represents arrivals/departures at/from node)
    public class Event implements Comparable<Event> {
        int routeID;
        int nodeRouteIdx;
        String type;
        LocalTime time;

        public Event(int routeID, int nodeRouteIdx, String type, LocalTime time) {
            this.routeID = routeID;
            this.nodeRouteIdx = nodeRouteIdx;
            this.type = type;
            this.time = time;
        }

        @Override
        public int compareTo(Event other){
            return time.compareTo(other.time);
        }
    }

    // Test instance that needs to be solved
    TestInstance testInstance;
    // Current solution
    int[][] currentSolutionRoutes;
    // Travel times
    int[] currentRouteTravelTimes;
    // Wait times
    int[] currentRouteWaitTimes;
    // wait times for nodes
	int[][] routeWaitTimesForNodes;


    public Solver(TestInstance testInstance) {
        // Assigning test instance
        this.testInstance = testInstance;
        this.currentSolutionRoutes = testInstance.routes;
        this.currentRouteTravelTimes = new int[this.testInstance.routeCount];
        this.currentRouteWaitTimes = new int[this.testInstance.routeCount];
        this.routeWaitTimesForNodes = new int[testInstance.routeCount][];
		for (int i = 0; i < testInstance.routeCount; i++) {
			routeWaitTimesForNodes[i] = new int[currentSolutionRoutes[i].length];
		}
		
        this.computeTotalDuration();
    }

    // Computes the total duration (travel + wait time) of all the routes in the current solution
    private void computeTotalDuration() {
        // Initialize the event queue with arrivals at the first node of each route
        // Sorted event queue
        PriorityQueue<Event> eventQueue = new PriorityQueue<Event>();
        for (int i = 0; i < this.currentSolutionRoutes.length; i++) {
            Event newEvent = new Event(i,0,"ARRIVAL",this.testInstance.routeStarts[i]);
            eventQueue.add(newEvent);
        }

        // Node queue contains departure times of both parked and waiting vehicles
        HashMap<Integer, ArrayList<LocalTime>> nodeQueues = new HashMap<>();

        // We process events (arrivals and departures) until the event queue is not empty
        while (eventQueue.size() > 0) {
            Event currentEvent = eventQueue.poll();

            // Create node queue for the current node if one does not exist
            int currentNodeID = this.currentSolutionRoutes[currentEvent.routeID][currentEvent.nodeRouteIdx];
            if (!nodeQueues.containsKey(currentNodeID)) {
                // All of the nodes are empty at the beginning (no parked vehicles, no waiting line)
                nodeQueues.put(currentNodeID, new ArrayList<>());
            }

            // Processing arrival and departure events until event queue is not empty
            if (currentEvent.type == "ARRIVAL") {
                // For arrival events we compute departure events using nodes' waiting queues and duration of the visits

                LocalTime newEventTime = null;
                if (nodeQueues.get(currentNodeID).size() < this.testInstance.nodeCapacities.get(currentNodeID)) {
                    // Node is not full
                    newEventTime = currentEvent.time.plusSeconds(this.testInstance.deliveryDurations.get(currentEvent.routeID).get(currentNodeID));
                    routeWaitTimesForNodes[currentEvent.routeID][currentEvent.nodeRouteIdx] = 0;
                } else {
                    // Node is full and waiting line is not empty
                    int waitingInLine = nodeQueues.get(currentNodeID).size() - this.testInstance.nodeCapacities.get(currentNodeID);
                    LocalTime waitUntil = nodeQueues.get(currentNodeID).get(waitingInLine);
                    newEventTime = waitUntil.plusSeconds(this.testInstance.deliveryDurations.get(currentEvent.routeID).get(currentNodeID));
                    this.currentRouteWaitTimes[currentEvent.routeID] += ChronoUnit.SECONDS.between(currentEvent.time, waitUntil);
                    routeWaitTimesForNodes[currentEvent.routeID][currentEvent.nodeRouteIdx] = (int) ChronoUnit.SECONDS
							.between(currentEvent.time, waitUntil);
                }
                // Adding new departure time to the node's queue
                nodeQueues.get(currentNodeID).add(newEventTime);
                Collections.sort(nodeQueues.get(currentNodeID));
                // Adding new departure event to the event queue
                Event newEvent = new Event(currentEvent.routeID, currentEvent.nodeRouteIdx, "DEPARTURE", newEventTime);
                eventQueue.add(newEvent);

            } else if (currentEvent.type == "DEPARTURE") {
                // For departure events we compute arrival events at the next node by adding travel time to the current time point

                nodeQueues.get(currentNodeID).remove(currentEvent.time);
                // If the current node is not the last in the route, create new arrival event
                if (this.currentSolutionRoutes[currentEvent.routeID].length > currentEvent.nodeRouteIdx + 1) {
                    int travelTime = this.testInstance.edgeCosts.get(currentNodeID).get(this.currentSolutionRoutes[currentEvent.routeID][currentEvent.nodeRouteIdx + 1]);
                    LocalTime newEventTime = currentEvent.time.plusSeconds(travelTime);
                    Event newEvent = new Event(currentEvent.routeID, currentEvent.nodeRouteIdx + 1, "ARRIVAL", newEventTime);
                    eventQueue.add(newEvent);
                    this.currentRouteTravelTimes[currentEvent.routeID] += travelTime;
                }
            }
        }
    }



}
