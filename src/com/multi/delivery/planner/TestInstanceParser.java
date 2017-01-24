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
            int startIdxSolutions = fileLines.indexOf("SOLUTION");

            // Splitting lines based on positions of the reference lines
            List<String> routeLines = fileLines.subList(startIdxRoutes+1, startIdxNodes);
            List<String> nodeLines = fileLines.subList(startIdxNodes+1, startIdxEdges);
            List<String> edgeLines = fileLines.subList(startIdxEdges+1, startIdxSolutions);
            List<String> solutionLines = fileLines.subList(startIdxSolutions+1,fileLines.size());

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
            int[][] routes = new int[routeCount][];
            HashMap<Integer, HashMap<Integer,Integer>> delivery_durations = new HashMap<>();
            LocalTime[] routeStarts = new LocalTime[routeCount];
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            for (int i = 0; i < routeLines.size(); i++) {
                String[] routeLineElements = routeLines.get(i).split(",");
                routeStarts[i] = LocalTime.parse(routeLineElements[0],timeFormatter);
                int[] stops = new int[routeLineElements.length-1];
                if (!delivery_durations.containsKey(i)) {
                    delivery_durations.put(i, new HashMap<>());
                }
                for (int j = 1; j < routeLineElements.length; j++) {
                    String[] stopElements = routeLineElements[j].split("\\|");
                    stops[j-1] = Integer.parseInt(stopElements[0]);
                    delivery_durations.get(i).put(stops[j-1], Integer.parseInt(stopElements[1]));
                }
                routes[i] = stops;
            }

            // TODO: Parse solution summary

            // Creating a new test instance with parsed settings
            newTestInstance = new TestInstance(nodeCount,routeCount,nodeIDs,nodeCapacities,edgeCosts,routes,delivery_durations,routeStarts);

        } catch (IOException e) {
            System.out.println("An error has occurred while reading test instance file \"" + filePath.toString() + "\"!");
            e.printStackTrace();
        }

        return newTestInstance;
    }

}
