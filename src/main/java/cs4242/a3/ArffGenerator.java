package cs4242.a3;

import static com.google.common.base.Preconditions.checkState;
import static cs4242.a3.StringUtil.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import weka.core.Instances;
import weka.core.converters.ArffSaver;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.primitives.Ints;

public final class ArffGenerator {

	private static Set<String> ids;

	private ArffGenerator() {
		// Private constructor, not meant to be instantiated
	}

	public static void main(String[] args) {

		List<FeatureVector> fvs = null;
		Instances data = null;
		long startTime = System.currentTimeMillis();

		try {

			fvs = extract();
			data = FeatureVector.getInstances(fvs, ids);
			saveArff(data);

		} catch (Exception e) {
			e.printStackTrace();
		}

		long elapsedTime = System.currentTimeMillis() - startTime;
		System.out.printf("Done! Run time: %ss\n", elapsedTime / 1000);

	}

	public static List<FeatureVector> extract() throws IOException {
		String inPath = System.getProperty("input.file.path");
		String outPath = System.getProperty("duplicates.file.path");
		int classIndex = Ints.tryParse(System.getProperty("col.index.class"));
		int idIndex = Ints.tryParse(System.getProperty("col.index.id"));
		int textIndex = Ints.tryParse(System.getProperty("col.index.text"));
		int lexicalErrorsIndex = Ints.tryParse(System
				.getProperty("col.index.lexerr"));
		final int NUMBER_OF_COLUMNS = 4;
		BufferedReader br = null;
		BufferedWriter bw = null;
		String line = "";
		String clazz = "";
		String id = "";
		String text = "";
		int lexicalErrors = 0;
		File inFile = new File(inPath);
		File outFile = new File(outPath);
		ids = new HashSet<String>();
		List<String> values = null;
		int duplicates = 0;
		int valuesSize = 0;
		StringBuilder sb;
		List<FeatureVector> data = new ArrayList<FeatureVector>(2000);
		System.out.printf("Extracting features...\n\t%s\n", inPath);

		try {

			br = new BufferedReader(new FileReader(inFile));
			bw = new BufferedWriter(new FileWriter(outFile));

			// Skip the header row

			br.readLine();

			while ((line = br.readLine()) != null) {

				values = Splitter.on(TAB_SEPARATOR).trimResults()
						.splitToList(line);

				valuesSize = values.size();

				checkState(valuesSize == NUMBER_OF_COLUMNS,
						"Expecting %s columns but found %s at line [%s]",
						NUMBER_OF_COLUMNS, valuesSize, line);

				clazz = values.get(classIndex);
				id = values.get(idIndex);
				text = values.get(textIndex);
				lexicalErrors = Ints.tryParse(values.get(lexicalErrorsIndex));

				if (ids.contains(id)) {
					sb = new StringBuilder(line);
					sb.append("\n");
					bw.write(sb.toString());
					duplicates++;
				} else {
					ids.add(id);
					data.add(new FeatureVector(clazz, id, text, lexicalErrors));

				}

			}

		} finally {
			if (br != null) {
				br.close();
			}
			if (bw != null) {
				bw.close();
			}
		}

		System.out.printf("Found %s duplicates\n", duplicates);
		if (duplicates > 0) {
			System.out.printf("Logged duplicates\n\t%s\n", outPath);
		}

		return data;
	}

	private static void saveArff(Instances data) throws IOException {
		ArffSaver arff = new ArffSaver();
		String filePath = System.getProperty("output.file.path");
		arff.setInstances(data);
		arff.setFile(new File(filePath));

		arff.writeBatch();
		System.out.printf("Saved %s attributes and %s instances:\n\t%s\n",
				data.numAttributes(), data.size(), filePath);

	}

}
