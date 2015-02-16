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
import weka.core.tokenizers.CharacterDelimitedTokenizer;
import weka.core.tokenizers.Tokenizer;
import weka.core.tokenizers.WordTokenizer;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

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
		ATTRIBUTES.add(new Attribute("classlabel"));
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
		Instances data = initDataset(inFilename);
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

			System.out.printf("Processed %s rows\n", count);

			data = stringToWordVector(data);

			saveArff(data, outFilePath(parentDir, inFilename));
			System.out.printf("Saved %s attributes and %s instances\n",
					data.numAttributes(), data.size());

			// svm(data);

		} catch (Exception e) {

			e.printStackTrace();
		} finally {
			br.close();
			System.out.println("Done!");
		}

	}

	private static Instances initDataset(String name) {
		Instances data = new Instances(name, ATTRIBUTES, NUMBER_OF_INSTANCES);
		data.setClass(ATTRIBUTES.get(0));

		return data;
	}

	private static Instances stringToWordVector(Instances in) throws Exception {
		StringToWordVector filter = new StringToWordVector();
		// filter.setOptions(options);
		filter.setLowerCaseTokens(true);
		filter.setInputFormat(in);
		
		WordTokenizer tokenizer = new WordTokenizer();
		
		// Removed exclamation mark, single quote (negation words)
		// 
		
		tokenizer.setDelimiters(" \r\n\t.,;:\"()?/\\&=%^~`|{}[]-1234567890");
		
		filter.setTokenizer(tokenizer);
		//filter.set
		Instances out = Filter.useFilter(in, filter);

		return out;
	}

	private static void svm(Instances data) throws Exception {
		// initialize svm classifier
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
