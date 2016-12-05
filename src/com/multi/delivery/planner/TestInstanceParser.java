package com.multi.delivery.planner;

import com.sun.tools.javac.util.ArrayUtils;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
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
            int startIdxNodes = fileLines.indexOf("NODES");
            int startIdxEdges = fileLines.indexOf("EDGES");
            int startIdxDeliverers = fileLines.indexOf("DELIVERERS");
            int startIdxSolutions = fileLines.indexOf("SOLUTION");

            // Splitting lines based on positions of the reference lines
            List<String> nodeLines = fileLines.subList(startIdxNodes+1, startIdxEdges);
            List<String> edgeLines = fileLines.subList(startIdxEdges+1, startIdxDeliverers);
            List<String> delivererLines = fileLines.subList(startIdxDeliverers+1, startIdxSolutions);
            List<String> solutionLines = fileLines.subList(startIdxSolutions+1,fileLines.size());

            // Parsing nodes
            // Node IDs, coordinates and capacities are stored in separated arrays
            int nodeCount = nodeLines.size();
            int[] nodeIDs = new int[nodeCount];
            Point2D[] nodeCoordinates = new Point2D[nodeCount];
            int[] nodeCapacities = new int[nodeCount];
            // We are also using HashMap to map nodes' original IDs (IDs from input file) with ordered IDs (positions in nodeIDs array)
            // The HashMap will be used for faster parsing of egde costs and deliverers
            HashMap<Integer,Integer> nodeIDsHashMap = new HashMap<>();

            for (int i = 0; i < nodeCount; i++) {
                String[] nodeLineElements = nodeLines.get(i).split(",");
                nodeIDs[i] = Integer.parseInt(nodeLineElements[0]);
                nodeIDsHashMap.put(nodeIDs[i],i);
                nodeCoordinates[i] = new Point();
                nodeCoordinates[i].setLocation(Double.parseDouble(nodeLineElements[1]),Double.parseDouble(nodeLineElements[2]));
                nodeCapacities[i] = Integer.parseInt(nodeLineElements[3]);
            }

            // Parsing edges
            // Edge costs are stored in 2-dim array
            float[][] edgeCosts = new float[nodeCount][nodeCount];
            for (int i = 0; i < edgeLines.size(); i++) {
                String[] edgeLineElements = edgeLines.get(i).split(",");
                Integer fromNodeID = Integer.parseInt(edgeLineElements[0]);
                Integer toNodeID = Integer.parseInt(edgeLineElements[1]);
                float cost = Float.parseFloat(edgeLineElements[2]);
                edgeCosts[nodeIDsHashMap.get(fromNodeID)][nodeIDsHashMap.get(toNodeID)] = cost;
            }

            // Parsing deliverers
            // Deliverers are stored in 2-dim array
            int delivererCount = delivererLines.size();
            int[][] deliverers = new int[delivererCount][];
            for (int i = 0; i < delivererLines.size(); i++) {
                String[] delivererLineElements = delivererLines.get(i).split(",");
                int[] newDeliverySet = new int[delivererLineElements.length-1];
                for (int j = 1; j < delivererLineElements.length; j++) {
                    newDeliverySet[j-1] = nodeIDsHashMap.get(Integer.parseInt(delivererLineElements[j]));
                }
                deliverers[i] = newDeliverySet;
            }

            // Parsing solutions
            String[] solutionSummary = solutionLines.get(0).split(",");
            float optimalScore = Float.parseFloat(solutionSummary[0]);
            float optimalMTSPScore = Float.parseFloat(solutionSummary[1]);
            int poolSize = Integer.parseInt(solutionSummary[2]);
            float computationTime = Float.parseFloat(solutionSummary[3]);
            int[][] solutions = new int[delivererCount][];
            for (int i = 1; i < solutionLines.size(); i++) {
                String[] solutionLineElements = solutionLines.get(i).split(",");
                int[] newDeliverySet = new int[solutionLineElements.length-1];
                for (int j = 1; j < solutionLineElements.length; j++) {
                    newDeliverySet[j-1] = nodeIDsHashMap.get(Integer.parseInt(solutionLineElements[j]));
                }
                solutions[i-1] = newDeliverySet;
            }

            // Creating a new test instance with parsed settings
            newTestInstance = new TestInstance(nodeCount,delivererCount,nodeIDs,edgeCosts,deliverers,
                    solutions,optimalScore,optimalMTSPScore,poolSize,computationTime);

        } catch (IOException e) {
            System.out.println("An error has occurred while reading test instance file \"" + filePath.toString() + "\"!");
            e.printStackTrace();
        }

        return newTestInstance;
    }

}
