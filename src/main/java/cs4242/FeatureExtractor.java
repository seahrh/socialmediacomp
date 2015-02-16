package cs4242;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import weka.classifiers.functions.LibSVM;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

public class FeatureExtractor {

	public static final ImmutableMap<String, Integer> CLASSES = ImmutableMap
			.<String, Integer> builder().put("positive", 1).put("negative", -1)
			.put("neutral", 0).build();

	public static final ImmutableList<String> INPUT_FIELDS = ImmutableList
			.<String> builder()
			.add("tweet id", "user id", "username", "content", "sentiment",
					"target", "annotator id").build();

	public static final ArrayList<Attribute> ATTRIBUTES = new ArrayList<Attribute>();

	static {
		List<String> values = null;
		ATTRIBUTES.add(new Attribute("class"));
		ATTRIBUTES.add(new Attribute("text", values));
	}

	public static final int NUMBER_OF_INSTANCES = 900;

	public static void main(String[] args) throws IOException {

		if (args.length != 1) {
			System.out.println("Usage: FeatureExtractor <input.tsv>");
			System.exit(1);
		}

		String inFilePath = args[0];
		String inFilename = Files.getNameWithoutExtension(inFilePath);
		File inFile = new File(inFilePath);

		String parentDir = inFile.getParent();
		BufferedReader br = new BufferedReader(new FileReader(inFile));
		String line;
		String sentiment;
		Optional<Integer> classValue;
		List<String> row;
		int count = 0;
		Instances data = new Instances(inFilename, ATTRIBUTES,
				NUMBER_OF_INSTANCES);
		data.setClass(ATTRIBUTES.get(0));
		Instance inst;

		try {

			// Skip the header row

			br.readLine();

			while ((line = br.readLine()) != null) {
				inst = new DenseInstance(2);
				inst.setDataset(data);
				row = Lists.newArrayList(Splitter.on('\t').trimResults()
						.split(line));

				sentiment = row.get(INPUT_FIELDS.indexOf("sentiment"));

				classValue = classValue(sentiment);

				if (classValue.isPresent()) {

					inst.setClassValue(classValue.get());
				} else {
					continue;
				}
				inst.setValue(ATTRIBUTES.get(1),
						row.get(INPUT_FIELDS.indexOf("content")));
				data.add(inst);

				count++;
			}

			saveArff(data, outFilePath(parentDir, inFilename));
			System.out.printf("Saved %s rows\n", count);
			
			svm(data);

		} catch (Exception e) {

			e.printStackTrace();
		} finally {
			br.close();
			System.out.println("Done!");
		}

	}
	
	private static void svm(Instances data) throws Exception {
		//initialize svm classifier
        LibSVM svm = new LibSVM();
        svm.buildClassifier(data);
	}

	private static Optional<Integer> classValue(String className) {
		Integer classValue = CLASSES.get(className);
		if (classValue != null) {
			return Optional.of(classValue);
		}
		return Optional.absent();
	}

	private static String outFilePath(String parentDir, String inFilename) {
		StringBuffer path = new StringBuffer(parentDir);

		path.append("\\");
		path.append(inFilename);
		path.append(".arff");
		return path.toString();
	}

	private static void saveArff(Instances data, String path)
			throws IOException {
		ArffSaver arff = new ArffSaver();
		arff.setInstances(data);
		arff.setFile(new File(path));

		arff.writeBatch();
	}

}
