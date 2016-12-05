package com.multi.delivery.planner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) {
        ArrayList<TestInstance> testInstances = new ArrayList<TestInstance>();

        // Parse all test instance files in "test_instances" folder
        try(Stream<Path> paths = Files.walk(Paths.get("./test_instances"))) {
            paths.forEach(filePath -> {
                if (Files.isRegularFile(filePath)) {
                    TestInstance newTestInstance = TestInstanceParser.parse(filePath);
                    testInstances.add(newTestInstance);
                }
            });
        } catch (IOException e) {
            System.out.println("An error has occurred while reading test instance files!");
            e.printStackTrace();
        }

        // Solving each of the test instances
        for (TestInstance testInstance : testInstances) {
            Solver newSolver = new Solver(testInstance);
            newSolver.solve();
        }


    }
}
