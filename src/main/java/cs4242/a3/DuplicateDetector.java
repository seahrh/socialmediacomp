package cs4242.a3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.primitives.Ints;

public final class DuplicateDetector {
	
	private static final CharMatcher SEPARATOR = CharMatcher.is('\t');
	
	private DuplicateDetector() {
		// Private constructor, not meant to be instantiated
	}

	public static void main(String[] args) throws IOException {
		String inPath = System.getProperty("input.file.path");
		String outPath = System.getProperty("output.file.path");
		int idIndex = Ints.tryParse(System.getProperty("target.index"));
		BufferedReader br = null;
		BufferedWriter bw = null;
		String line = "";
		String id = "";
		File inFile = new File(inPath);
		File outFile = new File(inPath);
		Set<String> knownIds = new HashSet<String>();
		List<String> values = null;
		long startTime = System.currentTimeMillis();
		
		System.out.printf("Input: %s\nOutput: %s\n", inPath, outPath);
		
		try {
			
			br = new BufferedReader(new FileReader(inFile));
			bw = new BufferedWriter(new FileWriter(outFile));
			
			// Skip the header row
			
			br.readLine();
			
			while ((line = br.readLine()) != null) {
				values = Splitter.on(SEPARATOR).trimResults().splitToList(line);
				id = values.get(idIndex);
				if (knownIds.contains(id)) {
					bw.write(line);
				} else {
					knownIds.add(id);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				br.close();
			}
			if (bw != null) {
				bw.close();
			}
		}

		long elapsedTime = System.currentTimeMillis() - startTime;
		System.out.printf("Found %s duplicates\nDone! Run time: %ss\n", knownIds.size(), elapsedTime / 1000);

	}

}
