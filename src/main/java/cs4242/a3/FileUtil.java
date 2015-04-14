package cs4242.a3;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public final class FileUtil {

	private FileUtil() {
		// Private constructor, not meant to be instantiated
	}

	public static void save(String content, String filePath) throws IOException {
		File file = new File(filePath);
		BufferedWriter bw = null;

		try {
			bw = new BufferedWriter(new FileWriter(file));
			bw.write(content);

		} finally {
			if (bw != null) {
				bw.close();
			}
		}
		System.out.printf("Saved file:\n\t%s\n", filePath);
	}

	public static void save(List<String> content, String filePath)
			throws IOException {
		File file = new File(filePath);
		BufferedWriter bw = null;

		try {
			bw = new BufferedWriter(new FileWriter(file));
			for (String line : content) {
				bw.write(line + "\n");
			}

		} finally {
			if (bw != null) {
				bw.close();
			}
		}
		System.out.printf("Saved %s lines in file:\n\t%s\n", content.size(), filePath);
	}
}
