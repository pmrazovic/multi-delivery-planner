package com.multi.delivery.planner;

import com.sun.tools.javac.util.ArrayUtils;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by pero on 05/12/2016.
 */
public class TestInstanceParser {

    public static TestInstance parse(Path filePath) {
        TestInstance newTestInstance = null;

        try {
            // Reading all lines at once
            List<String> fileLines = Files.readAllLines(filePath, Charset.defaultCharset());

            // Searching for reference lines
            int startIdxRoutes = fileLines.indexOf("ROUTES");
            int startIdxNodes = fileLines.indexOf("NODES");
            int startIdxEdges = fileLines.indexOf("EDGES");
            int startIdxComplexity = fileLines.indexOf("COMPLEXITY");
            int startIdxTspSolution = fileLines.indexOf("TSP SOLUTION");

            // Splitting lines based on positions of the reference lines
            List<String> routeLines = fileLines.subList(startIdxRoutes+1, startIdxNodes);
            List<String> nodeLines = fileLines.subList(startIdxNodes+1, startIdxEdges);
            List<String> edgeLines = fileLines.subList(startIdxEdges+1, startIdxComplexity);
            List<String> complexityLines = fileLines.subList(startIdxComplexity+1,startIdxTspSolution);
            List<String> tspSolutionLines = fileLines.subList(startIdxTspSolution+1,fileLines.size());

            // Parsing nodes
            // Node IDs are stored in arrays, and capacities in hash
            int nodeCount = nodeLines.size();
            int[] nodeIDs = new int[nodeCount];
            HashMap<Integer, Integer> nodeCapacities = new HashMap<>();

            for (int i = 0; i < nodeCount; i++) {
                String[] nodeLineElements = nodeLines.get(i).split(",");
                nodeIDs[i] = Integer.parseInt(nodeLineElements[0]);
                nodeCapacities.put(nodeIDs[i], Integer.parseInt(nodeLineElements[1]));
            }

            // Parsing edges
            // Edge costs are stored in hash
            HashMap<Integer, HashMap<Integer, Integer>> edgeCosts = new HashMap<>();
            for (int i = 0; i < edgeLines.size(); i++) {
                String[] edgeLineElements = edgeLines.get(i).split(",");
                Integer fromNodeID = Integer.parseInt(edgeLineElements[0]);
                Integer toNodeID = Integer.parseInt(edgeLineElements[1]);
                int cost = Integer.parseInt(edgeLineElements[2]);

                if (edgeCosts.containsKey(fromNodeID)) {
                    edgeCosts.get(fromNodeID).put(toNodeID,cost);
                } else {
                    edgeCosts.put(fromNodeID, new HashMap<Integer, Integer>());
                    edgeCosts.get(fromNodeID).put(toNodeID,cost);
                }
            }

            // Parsing routes
            // Routes are stored in 2dim array
            int routeCount = routeLines.size();
            ArrayList<ArrayList<Integer>> routes = new ArrayList<>();
            HashMap<Integer, HashMap<Integer,Integer>> delivery_durations = new HashMap<>();
            LocalTime[] routeStarts = new LocalTime[routeCount];
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            for (int i = 0; i < routeLines.size(); i++) {
                String[] routeLineElements = routeLines.get(i).split(",");
                routeStarts[i] = LocalTime.parse(routeLineElements[0],timeFormatter);
                ArrayList<Integer> stops = new ArrayList<>();
                if (!delivery_durations.containsKey(i)) {
                    delivery_durations.put(i, new HashMap<>());
                }
                for (int j = 1; j < routeLineElements.length; j++) {
                    String[] stopElements = routeLineElements[j].split("\\|");
                    stops.add(Integer.parseInt(stopElements[0]));
                    delivery_durations.get(i).put(stops.get(j-1), Integer.parseInt(stopElements[1]));
                }
                routes.add(stops);
            }

            // Parsing complexity information
            float instanceComplexity = Float.parseFloat(complexityLines.get(0));

            // Parsing TSP ordering
            ArrayList<ArrayList<Integer>> tspRoutes = new ArrayList<>();
            for (int i = 0; i < tspSolutionLines.size(); i++) {
                String[] tspLineElements = tspSolutionLines.get(i).split(",");
                ArrayList<Integer> stops = new ArrayList<>();
                for (int j = 0; j < tspLineElements.length; j++) {
                    stops.add(Integer.parseInt(tspLineElements[j]));
                }
                tspRoutes.add(stops);
            }

            // Creating a new test instance with parsed settings
            newTestInstance = new TestInstance(nodeCount,routeCount,nodeIDs,nodeCapacities,edgeCosts,routes,delivery_durations,routeStarts,tspRoutes,instanceComplexity);

        } catch (IOException e) {
            System.out.println("An error has occurred while reading test instance file \"" + filePath.toString() + "\"!");
            e.printStackTrace();
        }

        return newTestInstance;
    }

}
