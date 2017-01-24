package com.multi.delivery.planner;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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

    public Solver(TestInstance testInstance) {
        // Assigning test instance
        this.testInstance = testInstance;
        this.currentSolutionRoutes = testInstance.routes;
        this.currentRouteTravelTimes = new int[this.testInstance.routeCount];
        this.currentRouteWaitTimes = new int[this.testInstance.routeCount];
        this.computeTotalDuration();
    }

    // Computes the total duration (travel + wait time) of all the routes in the current solution
    private void computeTotalDuration() {
        // Initialize the event queue with arrivals at the first node of each route
        // Sorted event queue
        ArrayList<Event> eventQueue = new ArrayList<>();
        for (int i = 0; i < this.currentSolutionRoutes.length; i++) {
            Event newEvent = new Event(i,0,"ARRIVAL",this.testInstance.routeStarts[i]);
            eventQueue.add(newEvent);
        }
        Collections.sort(eventQueue);

        // Node queue contains departure times of both parked and waiting vehicles
        HashMap<Integer, ArrayList<LocalTime>> nodeQueues = new HashMap<>();

        // We process events (arrivals and departures) until the event queue is not empty
        while (eventQueue.size() > 0) {
            Event currentEvent = eventQueue.get(0);
            // Delete the current event from the queue
            eventQueue.remove(currentEvent);

            // Create node queue for the current node if one does not exist
            int currentNodeID = this.currentSolutionRoutes[currentEvent.routeID][currentEvent.nodeRouteIdx];
            if (!nodeQueues.containsKey(currentNodeID)) {
                // All of the nodes are empty at the beginning (no parked vehicles, no waiting line)
                nodeQueues.put(currentNodeID, new ArrayList<>());
            }

            if (currentEvent.type == "ARRIVAL") {
                LocalTime newEventTime = null;
                if (nodeQueues.get(currentNodeID).size() < this.testInstance.nodeCapacities.get(currentNodeID)) {
                    // Node is not full
                    newEventTime = currentEvent.time.plusSeconds(this.testInstance.deliveryDurations.get(currentEvent.routeID).get(currentNodeID));
                } else if (nodeQueues.get(currentNodeID).size() == this.testInstance.nodeCapacities.get(currentNodeID)) {
                    // Node is full and waiting line is empty
                    LocalTime waitUntil = nodeQueues.get(currentNodeID).get(0);
                    newEventTime = waitUntil.plusSeconds(this.testInstance.deliveryDurations.get(currentEvent.routeID).get(currentNodeID));
                    this.currentRouteWaitTimes[currentEvent.routeID] += ChronoUnit.SECONDS.between(currentEvent.time, waitUntil);
                    ;
                } else {
                    // Node is full and waiting line is not empty
                    int waitingInLine = nodeQueues.get(currentNodeID).size() - this.testInstance.nodeCapacities.get(nodeQueues);
                    LocalTime waitUntil = nodeQueues.get(currentNodeID).get(waitingInLine + 1);
                    newEventTime = waitUntil.plusSeconds(this.testInstance.deliveryDurations.get(currentEvent.routeID).get(currentNodeID));
                    this.currentRouteWaitTimes[currentEvent.routeID] += ChronoUnit.SECONDS.between(currentEvent.time, waitUntil);
                    ;
                }
                nodeQueues.get(currentNodeID).add(newEventTime);
                Collections.sort(nodeQueues.get(currentNodeID));
                Event newEvent = new Event(currentEvent.routeID, currentEvent.nodeRouteIdx, "DEPARTURE", newEventTime);
                eventQueue.add(newEvent);
            } else if (currentEvent.type == "DEPARTURE") {
                nodeQueues.get(currentNodeID).remove(currentEvent.time);
                if (this.currentSolutionRoutes[currentEvent.routeID].length > currentEvent.nodeRouteIdx + 1) {
                    int travelTime = this.testInstance.edgeCosts.get(currentNodeID).get(this.currentSolutionRoutes[currentEvent.routeID][currentEvent.nodeRouteIdx + 1]);
                    LocalTime newEventTime = currentEvent.time.plusSeconds(travelTime);
                    Event newEvent = new Event(currentEvent.routeID, currentEvent.nodeRouteIdx + 1, "ARRIVAL", newEventTime);
                    eventQueue.add(newEvent);
                    this.currentRouteTravelTimes[currentEvent.routeID] += travelTime;
                }
            }
            Collections.sort(eventQueue);
        }
    }



}
