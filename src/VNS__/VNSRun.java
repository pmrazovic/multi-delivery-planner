package VNS__;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.stream.Stream;

public class VNSRun {
	public static void main(String[] args) {

		// Parse all test instance files in "test_instances" folder
		try (Stream<Path> paths = Files.walk(Paths.get("./test_instances"))) {
			paths.forEach(filePath -> {
				if (Files.isRegularFile(filePath)) {
					VNS vns = new VNS(filePath, Integer.valueOf(args[0]), Integer.valueOf(args[1]),
							Double.valueOf(args[2]), Double.valueOf(args[3]));
					long time=System.currentTimeMillis();
					vns.runVNS();
					System.out.println((System.currentTimeMillis()-time)/1000);
					// System.out.println(vns.);
				}
			});
		} catch (IOException e) {
			System.out.println("An error has occurred while reading test instance files!");
			e.printStackTrace();
		}

	}

}
