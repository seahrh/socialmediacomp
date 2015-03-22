package cs4242.a2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;

public final class FileUtil {

	private FileUtil() {
		// Private constructor, not meant to be instantiated
	}

	public static void save(String output, String filePath) throws IOException {
		File file = new File(filePath);
		BufferedWriter bw = null;

		try {
			bw = new BufferedWriter(new FileWriter(file));
			bw.write(output);

		} finally {
			if (bw != null) {
				bw.close();
			}
		}
		System.out.printf("Saved file: %s\n", filePath);
	}

	public static void save(List<String> output, String filePath)
			throws IOException {
		File file = new File(filePath);
		BufferedWriter bw = null;

		try {
			bw = new BufferedWriter(new FileWriter(file));
			for (String line : output) {
				bw.write(line + "\n");
			}

		} finally {
			if (bw != null) {
				bw.close();
			}
		}
		System.out.printf("Saved %s lines: %s\n", output.size(), filePath);
	}
}
