package com.multi.delivery.planner;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) {
        ArrayList<TestInstance> testInstances = new ArrayList<TestInstance>();

        RBTree tree = new RBTree();
        tree.insertInterval(new int[]{1,3});
        tree.insertInterval(new int[]{2,6});
        tree.insertInterval(new int[]{2,5});
        tree.insertInterval(new int[]{3,8});
        tree.insertInterval(new int[]{3,6});

        RBTree.Node rmv_node_1 = tree.search(3,true);
        RBTree.Node rmv_node_2 = tree.search(6,false);

        rmv_node_1.remove();
        rmv_node_2.remove();

        System.out.println(tree.toString());

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
        }


    }
}
