package cs4242;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import weka.classifiers.functions.LibSVM;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ArffSaver;
import weka.core.stopwords.WordsFromFile;
import weka.core.tokenizers.WordTokenizer;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;

public final class ArffGenerator {

	private static final ImmutableMap<String, String> CLASSES = ImmutableMap
			.<String, String> builder().put("positive", "1")
			.put("negative", "-1").put("neutral", "0").build();

	private static final ImmutableList<String> INPUT_FIELDS = ImmutableList
			.<String> builder()
			.add("tweet id", "user id", "username", "content", "sentiment",
					"target", "annotator id").build();

	private static final ArrayList<Attribute> ATTRIBUTES = new ArrayList<Attribute>();

	private static final int NUMBER_OF_INSTANCES = 900;

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
		ATTRIBUTES.add(new Attribute("class_label", values)); // Index 0

		ATTRIBUTES.add(new Attribute("positive_strong")); // Index 1
		ATTRIBUTES.add(new Attribute("positive_weak")); // Index 2
		ATTRIBUTES.add(new Attribute("negative_strong")); // Index 3
		ATTRIBUTES.add(new Attribute("negative_weak")); // Index 4
		ATTRIBUTES.add(new Attribute("neutral_strong")); // Index 5
		ATTRIBUTES.add(new Attribute("neutral_weak")); // Index 6
		// ATTRIBUTES.add(new Attribute("posneg_strong")); // Index 7
		// ATTRIBUTES.add(new Attribute("posneg_weak")); // Index 8

		// Text attribute is a string

		values = null;
		ATTRIBUTES.add(new Attribute("text", values)); // Index 7

	}

	private ArffGenerator() {
		// Private constructor, not meant to be instantiated
	}

	public static void main(String[] args) throws IOException {

		long startTime = System.currentTimeMillis();
		long endTime;

		if (args.length != 5) {
			System.out
					.println("Usage: FeatureExtractor <input> <POS tagger> <sentiment lexicon> <negation words> <model output>");
			System.exit(1);
		}

		String inFilePath = args[0];
		String inFilename = Files.getNameWithoutExtension(inFilePath);
		File inFile = new File(inFilePath);

		// String stopwordsFilePath = args[1];

		String taggerPath = args[1];

		String lexiconPath = args[2];
		String negationPath = args[3];
		String modelPath = args[4];

		String parentDir = inFile.getParent();
		BufferedReader br = null;
		String line;
		List<Feature> features;
		String featuresString;
		String sentiment;
		Optional<String> classValue;
		List<String> row;
		int count = 0;
		Instances data = initDataset(inFilename);
		Instance inst;
		FeatureExtractor fe;
		List<String> tagged = new ArrayList<String>();
		int[] sentimentCount;
		StringBuffer sb;

		try {

			fe = new FeatureExtractor(taggerPath, lexiconPath, negationPath);

			br = new BufferedReader(new FileReader(inFile));

			// Skip the header row

			br.readLine();

			System.out.printf("Extracting features...\n\t%s\n", inFilePath);

			while ((line = br.readLine()) != null) {
				inst = new DenseInstance(ATTRIBUTES.size());
				inst.setDataset(data);
				row = Splitter.on('\t').trimResults().splitToList(line);

				sentiment = row.get(INPUT_FIELDS.indexOf("sentiment"));

				classValue = classValue(sentiment);

				if (classValue.isPresent()) {

					inst.setClassValue(classValue.get());
				} else {
					continue;
				}

				// Extract features

				features = fe.extract(row.get(INPUT_FIELDS.indexOf("content")));

				// Count sentiment words

				sentimentCount = FeatureExtractor.countSentiment(features);
				inst.setValue(ATTRIBUTES.get(1),
						sentimentCount[FeatureExtractor.STRONG_POSITIVE_INDEX]);
				inst.setValue(ATTRIBUTES.get(2),
						sentimentCount[FeatureExtractor.WEAK_POSITIVE_INDEX]);
				inst.setValue(ATTRIBUTES.get(3),
						sentimentCount[FeatureExtractor.STRONG_NEGATIVE_INDEX]);
				inst.setValue(ATTRIBUTES.get(4),
						sentimentCount[FeatureExtractor.WEAK_NEGATIVE_INDEX]);
				inst.setValue(ATTRIBUTES.get(5),
						sentimentCount[FeatureExtractor.STRONG_NEUTRAL_INDEX]);
				inst.setValue(ATTRIBUTES.get(6),
						sentimentCount[FeatureExtractor.WEAK_NEUTRAL_INDEX]);
				// inst.setValue(ATTRIBUTES.get(7),
				// sentimentCount[FeatureExtractor.STRONG_POSNEG_INDEX]);
				// inst.setValue(ATTRIBUTES.get(8),
				// sentimentCount[FeatureExtractor.WEAK_POSNEG_INDEX]);

				// Concat features into string delimited by whitespace

				featuresString = Feature.toString(features);
				inst.setValue(ATTRIBUTES.get(7), featuresString);

				// Debug output

				sb = new StringBuffer(classValue.get());
				sb.append(" ");
				sb.append(FeatureExtractor
						.countSentimentToString(sentimentCount));
				sb.append(featuresString);
				tagged.add(sb.toString());

				data.add(inst);

				count++;
			}

			System.out.printf("Features extracted. Processed %s rows\n", count);

			save(fe.pruned(), FeatureExtractor.PRUNED_POS_FILE);
			save(tagged, FeatureExtractor.TAGGED_POS_FILE);

			data = stringToWordVector(data);

			saveArff(data, outFilePath(parentDir, inFilename));
			System.out.printf("Saved %s attributes and %s instances\n",
					data.numAttributes(), data.size());

			// svm(data, modelPath);

			// loadModel(modelPath);

			endTime = System.currentTimeMillis();
			System.out.printf("Done! Run time: %ss\n",
					(endTime - startTime) / 1000);

		} catch (Exception e) {

			e.printStackTrace();
		} finally {
			if (br != null) {
				br.close();
			}
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
		// filter.setLowerCaseTokens(true);
		filter.setInputFormat(in);

		filter.setTokenizer(tokenizer());

		// TODO should we still use stopwords?
		// filter.setStopwordsHandler(stopwords(stopwordsFilePath));
		filter.setAttributeIndices("last");

		Instances out = Filter.useFilter(in, filter);

		return out;
	}

	private static WordTokenizer tokenizer() {
		WordTokenizer tokenizer = new WordTokenizer();
		tokenizer.setDelimiters(" \r\n\t");
		return tokenizer;
	}

	private static WordsFromFile stopwords(String path) throws IOException {
		WordsFromFile stopwords = new WordsFromFile();
		stopwords.setStopwords(new File(path));
		return stopwords;

	}

	private static void svm(Instances data, String filePath) throws Exception {
		// initialize svm classifier
		LibSVM svm = new LibSVM();
		// svm.setModelFile(new File(modelPath));
		System.out.println("Training SVM classifier...");
		svm.buildClassifier(data);
		System.out.println("Trained SVM classifier");
		SerializationHelper.write(filePath, svm);
		System.out.printf("Saved classifier to model file:\n\t%s\n", filePath);
	}

	private static LibSVM loadModel(String filePath) throws Exception {
		System.out.printf("Loading SVM classifier from file\n\t%s\n", filePath);
		LibSVM svm = (LibSVM) SerializationHelper.read(filePath);
		System.out.println("Loaded SVM classifer.");
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

	public static void save(List<String> output, String filePath)
			throws IOException {
		File file = new File(filePath);
		BufferedWriter bw = null;

		try {
			bw = new BufferedWriter(new FileWriter(file));
			for (String item : output) {
				bw.write(item + "\n");
			}
			System.out.printf("Saved %s items: %s\n", output.size(), filePath);
		} finally {
			if (bw != null) {
				bw.close();
			}
		}
	}

}
