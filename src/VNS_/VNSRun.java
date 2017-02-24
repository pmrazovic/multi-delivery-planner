package VNS_;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class VNSRun {
	public static void main(String[] args) {

		// Parse all test instance files in "test_instances" folder
		try (Stream<Path> paths = Files.walk(Paths.get("./test_instances"))) {
			paths.forEach(filePath -> {
				if (Files.isRegularFile(filePath)) {
					VNS vns = new VNS(filePath, 100, 10, 0.4, 0.3);
					vns.runVNS();
					Solver solver = vns.getSolver();
					System.out.println(solver);
				}
			});
		} catch (IOException e) {
			System.out.println("An error has occurred while reading test instance files!");
			e.printStackTrace();
		}

	}

}
