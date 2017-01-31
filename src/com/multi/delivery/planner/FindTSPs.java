package com.multi.delivery.planner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Stream;

import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class FindTSPs {

	public static void main(String[] args) {
		ArrayList<TestInstance> testInstances = new ArrayList<TestInstance>();
		// need to add cplex jar in ibm folder to the project
		try (Stream<Path> paths = Files.walk(Paths.get("./test_instances"))) {
			paths.forEach(filePath -> {
				if (Files.isRegularFile(filePath)) {
					TestInstance newTestInstance = TestInstanceParser.parse(filePath);
					testInstances.add(newTestInstance);
					String result = "";
					for (int[] route : newTestInstance.routes) {
						// now for elimination the having duplication
						String txtRoute = "TSP SOLUTIONS\n";

						// the newRoute thing is for the routes with repeats,
						// after we remove them, we can discard the part
						ArrayList<Integer> routeList = new ArrayList<Integer>();
						for (int r : route)
							if (!routeList.contains(r))
								routeList.add(r);
						int[] newRoute = new int[routeList.size()];

						for (int r = 0; r < newRoute.length; r++)
							newRoute[r] = routeList.get(r);
						// end of repeating elements remove part

						// find the tsp route
						int[] tspRoute = tspRoute(newRoute, newTestInstance.edgeCosts);

						for (int v : tspRoute) {
							txtRoute += v + ",";
						}
						result += txtRoute + "\n";
					}
					try {
						Files.write(filePath, result.getBytes(), StandardOpenOption.APPEND);
					} catch (Exception e) {
						e.printStackTrace();
					}
					System.out.println(result);

				}
			});
		} catch (IOException e) {
			System.out.println("An error has occurred while reading test instance files!");
			e.printStackTrace();
		}

	}

	private static int[] tspRoute(int[] route, HashMap<Integer, HashMap<Integer, Integer>> edgeCosts) {
		// size of route
		int n = route.length;
		int[] tspRoute = new int[n];
		try {
			IloCplex cplex = new IloCplex();

			// variables
			// x
			IloNumVar[][] x = new IloNumVar[n][];
			for (int i = 0; i < n; i++) {
				// n dimensioned boolean variable to say which j is chosen
				// from
				// mthe city i
				// for xij
				x[i] = cplex.boolVarArray(n);
			}

			// u - value from 1 to n
			IloNumVar[] u = cplex.intVarArray(n, 1, n);

			// objective formula
			IloLinearNumExpr obj = cplex.linearNumExpr();
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (j != i) {
						obj.addTerm(edgeCosts.get(route[i]).get(route[j]), x[i][j]);
					}
				}
			}

			// minimization
			cplex.addMinimize(obj);

			for (int j = 0; j < n; j++) {
				// for constraint chosen only by one, in for one city
				IloLinearNumExpr expr = cplex.linearNumExpr();
				for (int i = 0; i < n; i++) {
					if (i != j)
						expr.addTerm(1.0, x[i][j]);
				}
				cplex.addEq(expr, 1.0);

			}

			for (int i = 0; i < n; i++) {
				// for constraint chosen only by one, in for one city
				IloLinearNumExpr expr = cplex.linearNumExpr();
				for (int j = 0; j < n; j++) {
					if (i != j)
						expr.addTerm(1.0, x[i][j]);
				}
				cplex.addEq(expr, 1.0);

			}

			// u constraint
			for (int i = 1; i < n; i++) {
				for (int j = 1; j < n; j++) {
					if (i != j) {
						IloLinearNumExpr exp = cplex.linearNumExpr();
						exp.addTerm(1.0, u[i]);
						exp.addTerm(-1.0, u[j]);
						exp.addTerm(n - 1, x[i][j]);
						cplex.addLe(exp, n - 2);
					}
				}
			}

			// only first one can get the value 1
			for (int i = 1; i < n; i++) {
				IloLinearNumExpr exp = cplex.linearNumExpr();
				exp.addTerm(1.0, u[i]);
				cplex.addGe(exp, 2);

			}

			// cplex.setParam(IloCplex.IntParam.SolnPoolCapacity, 24);
			// cplex.setParam(IloCplex.IntParam.SolnPoolIntensity, 4);
			// cplex.setParam(IloCplex.IntParam.PopulateLim, 100);
			if (cplex.solve()) {
				tspRoute[0] = route[0];
				System.out.println(cplex.getObjValue());
				for (int i = 1; i < route.length; i++) {
					System.out.print(cplex.getValue(u[i]) + " ");
					tspRoute[(int) (cplex.getValue(u[i]) - 1)] = route[i];
				}

				System.out.println();

				// int it = 0, jt = 0;
				// for (int i = 0; i < n; i++) {
				// for (int j = 0; j < n; j++) {
				// // System.out.println(x[i][j].getLB());
				// if (i != j) {
				//
				// System.out.print(cplex.getValue(x[i][j], 0) + "\t");
				//
				// } else
				// System.out.print("---\t");
				// }
				// System.out.println();
				// }

			} else
				System.out.println("no sol");
			// System.out.println(cplex.getObjValue());
			cplex.end();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tspRoute;
	}

}
