package cs4242;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import weka.classifiers.Classifier;
import weka.classifiers.functions.LibSVM;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ArffSaver;
import weka.core.stopwords.WordsFromFile;
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

	public static final ImmutableMap<String, String> CLASSES = ImmutableMap
			.<String, String> builder().put("positive", "1")
			.put("negative", "-1").put("neutral", "0").build();

	public static final ImmutableList<String> INPUT_FIELDS = ImmutableList
			.<String> builder()
			.add("tweet id", "user id", "username", "content", "sentiment",
					"target", "annotator id").build();

	public static final ArrayList<Attribute> ATTRIBUTES = new ArrayList<Attribute>();

	static {
		// Class label attribute is nominal

		// There is a known problem saving SparseInstance objects from datasets
		// that have string attributes. In Weka, string and nominal data values
		// are stored as numbers; these numbers act as indexes into an array of
		// possible attribute values (this is very efficient). However, the
		// first string value is assigned index 0: this means that, internally,
		// this value is stored as a 0. When a SparseInstance is written, string
		// instances with internal value 0 are not output, so their string value
		// is lost (and when the arff file is read again, the default value 0 is
		// the index of a different string value, so the attribute value appears
		// to change). To get around this problem, add a dummy string value at
		// index 0 that is never used whenever you declare string attributes
		// that are likely to be used in SparseInstance objects and saved as
		// Sparse ARFF files.

		List<String> values = ImmutableList.<String> builder()
				.add("dummy", "1", "0", "-1").build();
		ATTRIBUTES.add(new Attribute("classlabel", values));

		// Text attribute is a string

		values = null;
		ATTRIBUTES.add(new Attribute("text", values));
	}

	public static final int NUMBER_OF_INSTANCES = 900;

	public static void main(String[] args) throws IOException {

		if (args.length != 3) {
			System.out.println("Usage: FeatureExtractor <input> <stopwords> <output model>");
			System.exit(1);
		}

		String inFilePath = args[0];
		String inFilename = Files.getNameWithoutExtension(inFilePath);
		File inFile = new File(inFilePath);

		String stopwordsFilePath = args[1];
		String modelFilePath = args[2];

		String parentDir = inFile.getParent();
		BufferedReader br = null;
		String line;
		String sentiment;
		Optional<String> classValue;
		List<String> row;
		int count = 0;
		Instances data = initDataset(inFilename);
		Instance inst;

		try {

			br = new BufferedReader(new FileReader(inFile));

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
				
				//TODO Normalize the string before tokenizing
				
				inst.setValue(ATTRIBUTES.get(1),
						row.get(INPUT_FIELDS.indexOf("content")));
				data.add(inst);

				count++;
			}

			System.out.printf("Processed %s rows\n", count);

			data = stringToWordVector(data, stopwordsFilePath);

			saveArff(data, outFilePath(parentDir, inFilename));
			System.out.printf("Saved %s attributes and %s instances\n",
					data.numAttributes(), data.size());

			svm(data, modelFilePath);
			
			loadModel(modelFilePath);

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

	private static Instances stringToWordVector(Instances in,
			String stopwordsFilePath) throws Exception {
		StringToWordVector filter = new StringToWordVector();
		// filter.setOptions(options);
		filter.setLowerCaseTokens(true);
		filter.setInputFormat(in);

		filter.setTokenizer(tokenizer());
		filter.setStopwordsHandler(stopwords(stopwordsFilePath));
		filter.setAttributeIndices("2");

		Instances out = Filter.useFilter(in, filter);

		return out;
	}

	private static WordTokenizer tokenizer() {
		WordTokenizer tokenizer = new WordTokenizer();

		// Removed exclamation mark, single quote (negation words)
		// TODO emoticons

		tokenizer.setDelimiters(" \r\n\t.,;:\"()?/\\&=%^~`|{}[]-1234567890");
		return tokenizer;
	}

	private static WordsFromFile stopwords(String path) throws IOException {
		WordsFromFile stopwords = new WordsFromFile();
		stopwords.setStopwords(new File(path));
		return stopwords;

	}

	private static void svm(Instances data, String modelPath) throws Exception {
		// initialize svm classifier
		LibSVM svm = new LibSVM();
		//svm.setModelFile(new File(modelPath));
		svm.buildClassifier(data);
		SerializationHelper.write(modelPath, svm);
		System.out.printf("SVM classifier trained\nModel file saved: %s\n", modelPath);
	}
	
	private static LibSVM loadModel(String path) throws Exception {
		LibSVM svm = (LibSVM)SerializationHelper.read(path);
		System.out.println("SVM classifer loaded from file successfully.");
		return svm;
	}

	private static Optional<String> classValue(String className) {
		String classValue = CLASSES.get(className);
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
