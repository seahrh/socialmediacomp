package cs4242.a3;

import static cs4242.a3.StringUtil.trim;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	
	public static Set<String> load(String filePath) {
		Set<String> result = new HashSet<String>();
		
		File file = new File(filePath);
		
		BufferedReader br = null;
		String line = "";

		try {
			br = new BufferedReader(new FileReader(file));
			while ((line = br.readLine()) != null) {
				line = trim(line);
				result.add(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e.getMessage(), e);
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new IllegalStateException(
						"Buffered reader cannot be closed", e);
			}
		}

		

		return result;
	}
}
