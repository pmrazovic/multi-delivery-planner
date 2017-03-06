package com.multi.delivery.planner;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Solution {
    // Constants
    public static final String ARRIVAL = "arrival_event";
    public static final String DEPARTURE = "departure_event";

    // Corresponding test instance
    TestInstance testInstance;
    // Routes
    ArrayList<ArrayList<Integer>> routes;
    // Travel times
    int[] travelTimesPerRoute;
    // Waiting times per route
    int[] waitTimesPerRoute;
    // Waiting times per node
    //int[][] waitTimesPerNode;
    // Sorted list of waiting objects
    ArrayList<Waiting> nodeWaitings;
    // Total cost
    int totalCost;

    // Constructor 1 - routes are not given
    public Solution(TestInstance testInstance) {
        this.testInstance = testInstance;
        this.routes = this.testInstance.routes;
        this.travelTimesPerRoute = new int[this.testInstance.routeCount];
        this.waitTimesPerRoute = new int[this.testInstance.routeCount];
        this.nodeWaitings = new ArrayList<>();
        computeTimeCosts();
    }

    // Constructor 2 - routes are given
    public Solution(TestInstance testInstance, ArrayList<ArrayList<Integer>> routes) {
        this.testInstance = testInstance;
        this.routes = routes;
        this.travelTimesPerRoute = new int[this.testInstance.routeCount];
        this.waitTimesPerRoute = new int[this.testInstance.routeCount];
        this.nodeWaitings = new ArrayList<>();
        computeTimeCosts();
    }

    // Event class (represents arrivals/departures at/from node)
    private class Event implements Comparable<Event> {
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

    // Class Waiting represents waitings at a specific position in a route
    public class Waiting implements Comparable<Waiting> {
        int routeIdx;
        int nodeRouteIdx;
        int waitAmount;

        public Waiting(int routeIdx, int nodePositionIdx, int waitAmount) {
            this.routeIdx = routeIdx;
            this.nodeRouteIdx = nodePositionIdx;
            this.waitAmount = waitAmount;
        }

        @Override
        public int compareTo(Waiting other) {
            return Integer.compare(other.waitAmount,this.waitAmount);
        }
    }

    // Computes the total duration (travel + wait time) of all the routes in the current solution
    public void computeTimeCosts() {
        // Initialize the event queue with arrivals at the first node of each route
        // Sorted event queue
        PriorityQueue<Event> eventQueue = new PriorityQueue<>();
        for (int i = 0; i < this.routes.size(); i++) {
            Event newEvent = new Event(i,0,ARRIVAL,this.testInstance.routeStarts[i]);
            eventQueue.add(newEvent);
        }

        // Node queue contains departure times of both parked and waiting vehicles
        HashMap<Integer, ArrayList<LocalTime>> nodeQueues = new HashMap<>();

        // We process events (arrivals and departures) until the event queue is not empty
        while (eventQueue.size() > 0) {
            Event currentEvent = eventQueue.poll();

            // Create node queue for the current node if one does not exist
            int currentNodeID = this.routes.get(currentEvent.routeID).get(currentEvent.nodeRouteIdx);
            if (!nodeQueues.containsKey(currentNodeID)) {
                // All of the nodes are empty at the beginning (no parked vehicles, no waiting line)
                nodeQueues.put(currentNodeID, new ArrayList<>());
            }

            // Processing arrival and departure events until event queue is not empty
            if (currentEvent.type.equals(ARRIVAL)) {
                // For arrival events we compute departure events using nodes' waiting queues and duration of the visits

                LocalTime newEventTime;
                if (nodeQueues.get(currentNodeID).size() < this.testInstance.nodeCapacities.get(currentNodeID)) {
                    // Node is not full
                    newEventTime = currentEvent.time.plusSeconds(this.testInstance.deliveryDurations.get(currentEvent.routeID).get(currentNodeID));
                } else {
                    // Node is full and waiting line is not empty
                    int waitingInLine = nodeQueues.get(currentNodeID).size() - this.testInstance.nodeCapacities.get(currentNodeID);
                    LocalTime waitUntil = nodeQueues.get(currentNodeID).get(waitingInLine);
                    newEventTime = waitUntil.plusSeconds(this.testInstance.deliveryDurations.get(currentEvent.routeID).get(currentNodeID));

                    int newWaitingTime = (int) ChronoUnit.SECONDS.between(currentEvent.time, waitUntil);
                    nodeWaitings.add(new Waiting(currentEvent.routeID, currentEvent.nodeRouteIdx, newWaitingTime));
                    this.waitTimesPerRoute[currentEvent.routeID] += newWaitingTime;
                    this.totalCost += newWaitingTime + this.testInstance.deliveryDurations.get(currentEvent.routeID).get(currentNodeID);
                }
                // Adding new departure time to the node's queue
                nodeQueues.get(currentNodeID).add(newEventTime);
                Collections.sort(nodeQueues.get(currentNodeID));
                // Adding new departure event to the event queue
                Event newEvent = new Event(currentEvent.routeID, currentEvent.nodeRouteIdx, DEPARTURE, newEventTime);
                eventQueue.add(newEvent);

            } else if (currentEvent.type.equals(DEPARTURE)) {
                // For departure events we compute arrival events at the next node by adding travel time to the current time point

                nodeQueues.get(currentNodeID).remove(currentEvent.time);
                // If the current node is not the last in the route, create new arrival event
                if (this.routes.get(currentEvent.routeID).size() > currentEvent.nodeRouteIdx + 1) {
                    int travelTime = this.testInstance.edgeCosts.get(currentNodeID).get(this.routes.get(currentEvent.routeID).get(currentEvent.nodeRouteIdx + 1));
                    LocalTime newEventTime = currentEvent.time.plusSeconds(travelTime);
                    Event newEvent = new Event(currentEvent.routeID, currentEvent.nodeRouteIdx + 1, ARRIVAL, newEventTime);
                    eventQueue.add(newEvent);
                    this.travelTimesPerRoute[currentEvent.routeID] += travelTime;
                    this.totalCost += travelTime;
                }
            }
        }

        // Sorting node waitings
        Collections.sort(nodeWaitings);

    }
}
