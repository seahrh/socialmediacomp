package cs4242.a1;

import static com.google.common.base.Preconditions.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import weka.classifiers.functions.LibSVM;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.SparseInstance;
import weka.core.converters.ArffSaver;
import weka.core.stopwords.WordsFromFile;
import weka.core.tokenizers.WordTokenizer;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
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

	private static final int NUMBER_OF_INSTANCES = 2000;

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

		// Aspect attribute is nominal

		values = ImmutableList
				.<String> builder()
				.add("dummy", "other", "teaparty", "dems", "hcr", "gop",
						"conservatives", "stupak", "liberals", "obama").build();
		ATTRIBUTES.add(new Attribute("aspect", values)); // Index 1

		ATTRIBUTES.add(new Attribute("positive_strong")); // Index 2
		ATTRIBUTES.add(new Attribute("positive_weak")); // Index 3
		ATTRIBUTES.add(new Attribute("negative_strong")); // Index 4
		ATTRIBUTES.add(new Attribute("negative_weak")); // Index 5
		ATTRIBUTES.add(new Attribute("neutral_strong")); // Index 6
		ATTRIBUTES.add(new Attribute("neutral_weak")); // Index 7
		// ATTRIBUTES.add(new Attribute("posneg_strong"));
		// ATTRIBUTES.add(new Attribute("posneg_weak"));

		values = ImmutableList.<String> builder().add("dummy", "train", "dev")
				.build();
		ATTRIBUTES.add(new Attribute("dataset", values)); // Index 8

		// Text attribute is a string

		values = null;
		ATTRIBUTES.add(new Attribute("text", values)); // Index 9

	}

	private ArffGenerator() {
		// Private constructor, not meant to be instantiated
	}

	public static void main(String[] args) throws IOException {

		long startTime = System.currentTimeMillis();
		long endTime;

		if (args.length != 7) {
			System.out
					.println("Usage: FeatureExtractor <train set> <dev set> <test set> <POS tagger> <sentiment lexicon> <negation words> <model output>");
			System.exit(1);
		}

		String trainPath = args[0];
		String devPath = args[1];
		String testPath = args[2];
		String taggerPath = args[3];
		String lexiconPath = args[4];
		String negationPath = args[5];
		String modelPath = args[6];

		FeatureExtractor fe;

		Instances trainData;
		Instances testData;

		try {

			fe = new FeatureExtractor(taggerPath, lexiconPath, negationPath);

			trainData = trainData(trainPath, devPath, fe);

			testData = testData(testPath, trainData, fe);

			save(fe.pruned(), FeatureExtractor.PRUNED_POS_FILE);

			// svm(trainData, modelPath);

			// loadModel(modelPath);

		} catch (Exception e) {

			e.printStackTrace();
		}
		endTime = System.currentTimeMillis();
		System.out
				.printf("Done! Run time: %ss\n", (endTime - startTime) / 1000);
	}

	private static Instances testData(String testPath, Instances header,
			FeatureExtractor fe) throws Exception {
		Instances out = load(testPath, fe);
		out = stringToWordVector(out);
		out = setHeader(out, header, testPath);
		String parentDir = new File(testPath).getParent();
		saveArff(out, outFilePath(parentDir, "test", "arff"));
		return out;
	}

	private static Instances trainData(String trainPath, String devPath,
			FeatureExtractor fe) throws Exception {
		Instances trainData;
		Instances devData;
		Instances trainHeader;

		trainHeader = load(trainPath, "train", fe);
		trainHeader.addAll(load(devPath, "dev", fe));

		trainHeader = stringToWordVector(trainHeader);
		trainData = dataset("train", trainHeader);
		devData = dataset("dev", trainHeader);

		trainHeader = removeDatasetName(trainHeader);
		trainData = removeDatasetName(trainData);
		devData = removeDatasetName(devData);

		String parentDir = new File(trainPath).getParent();
		saveArff(trainData, outFilePath(parentDir, "train", "arff"));
		saveArff(devData, outFilePath(parentDir, "dev", "arff"));

		return trainHeader;
	}

	private static Instances setHeader(Instances in, Instances header,
			String filePath) throws IOException {
		checkNotNull(header, "Instances header cannot be null");

		Instances out = new Instances(header, in.numInstances());

		List<Attribute> commonAttrs = new ArrayList<Attribute>();
		List<String> commonAttrsDebug = new ArrayList<String>();

		Attribute attr;
		SparseInstance inst;

		for (int i = 0; i < in.numAttributes(); i++) {
			attr = in.attribute(i);
			if (header.attribute(attr.name()) != null) {
				// Attribute exists in both header and input data

				commonAttrs.add(attr);
				commonAttrsDebug.add(attr.name());
			}
		}

		System.out
				.printf("Train header has %s attributes, out of which %s are common with test dataset\n",
						header.numAttributes(), commonAttrs.size());

		String parentDir = new File(filePath).getParent();
		save(commonAttrsDebug, outFilePath(parentDir, "common_attr", "txt"));

		for (Instance input : in) {

			inst = new SparseInstance(1d, new double[header.numAttributes()]);
			inst.setDataset(header);
			for (Attribute ca : commonAttrs) {
				if (input.isMissing(ca)) {
					inst.setMissing(ca);
				} else {
					inst.setValue(ca, input.value(ca));
				}

			}

			out.add(inst);
		}

		return out;
	}

	private static Instances dataset(String name, Instances header) {
		checkNotNull(header, "Instances header cannot be null");

		Instances dataset = new Instances(header, header.numInstances());
		Attribute datasetName = header.attribute("dataset");

		checkNotNull(datasetName,
				"Missing attribute in instances header: datasetName");

		for (Instance i : header) {
			if (i.value(datasetName) == datasetName.indexOfValue(name)) {
				dataset.add(i);
			}
		}

		return dataset;
	}

	private static Instances removeDatasetName(Instances in) {
		Attribute datasetName = in.attribute("dataset");
		// Remove the 'dataset name' attribute, not required for
		// training/testing

		in.deleteAttributeAt(datasetName.index());
		return in;
	}

	private static Instances load(String filePath, FeatureExtractor fe)
			throws IOException {
		return load(filePath, null, fe);
	}

	private static Instances load(String filePath, String datasetName,
			FeatureExtractor fe) throws IOException {
		datasetName = Strings.nullToEmpty(datasetName);
		File file = new File(filePath);
		String filename = Files.getNameWithoutExtension(filePath);
		String parentDir = file.getParent();
		String relationName = "train";
		Instances data = new Instances(relationName, ATTRIBUTES,
				NUMBER_OF_INSTANCES);
		data.setClass(ATTRIBUTES.get(0));
		BufferedReader br = null;
		String line;
		List<Feature> features;
		String featuresString;
		String sentiment;
		Attribute aspect;
		String aspectStr;
		Optional<String> classValue;
		List<String> row;
		int count = 0;
		Instance inst;
		List<String> tagged = new ArrayList<String>();
		int[] sentimentCount;
		StringBuffer sb;
		Set<String> aspects = new HashSet<String>();
		try {

			br = new BufferedReader(new FileReader(file));

			// Skip the header row

			br.readLine();

			System.out.printf("Extracting features...\n\t%s\n", filePath);

			while ((line = br.readLine()) != null) {
				inst = new DenseInstance(ATTRIBUTES.size());
				inst.setDataset(data);
				row = Splitter.on('\t').trimResults().splitToList(line);

				// Sentiment is the class variable

				sentiment = row.get(INPUT_FIELDS.indexOf("sentiment"));
				classValue = classValue(sentiment);
				if (classValue.isPresent()) {
					inst.setClassValue(classValue.get());
				} else {
					continue;
				}

				// Target (aspect) is the alternative class variable
				// Test set may have more aspects than train and dev sets
				// Ignore new aspects, set value as missing

				aspect = ATTRIBUTES.get(1);
				aspectStr = row.get(INPUT_FIELDS.indexOf("target"));
				if (aspectStr.isEmpty()) {
					inst.setMissing(aspect);
				} else {
					aspects.add(aspectStr);

					if (aspect.indexOfValue(aspectStr) == -1) {
						inst.setMissing(aspect);
					} else {
						inst.setValue(aspect, aspectStr);
					}
				}

				// Extract features

				features = fe.extract(row.get(INPUT_FIELDS.indexOf("content")));

				// Count sentiment words

				sentimentCount = FeatureExtractor.countSentiment(features);
				inst.setValue(ATTRIBUTES.get(2),
						sentimentCount[FeatureExtractor.STRONG_POSITIVE_INDEX]);
				inst.setValue(ATTRIBUTES.get(3),
						sentimentCount[FeatureExtractor.WEAK_POSITIVE_INDEX]);
				inst.setValue(ATTRIBUTES.get(4),
						sentimentCount[FeatureExtractor.STRONG_NEGATIVE_INDEX]);
				inst.setValue(ATTRIBUTES.get(5),
						sentimentCount[FeatureExtractor.WEAK_NEGATIVE_INDEX]);
				inst.setValue(ATTRIBUTES.get(6),
						sentimentCount[FeatureExtractor.STRONG_NEUTRAL_INDEX]);
				inst.setValue(ATTRIBUTES.get(7),
						sentimentCount[FeatureExtractor.WEAK_NEUTRAL_INDEX]);
				// inst.setValue(ATTRIBUTES.get(7),
				// sentimentCount[FeatureExtractor.STRONG_POSNEG_INDEX]);
				// inst.setValue(ATTRIBUTES.get(8),
				// sentimentCount[FeatureExtractor.WEAK_POSNEG_INDEX]);

				// Concat features into string delimited by whitespace

				featuresString = Feature.toString(features);
				inst.setValue(ATTRIBUTES.get(ATTRIBUTES.size() - 1),
						featuresString);

				// Set the dataset name

				if (datasetName.isEmpty()) {
					inst.setMissing(ATTRIBUTES.get(ATTRIBUTES.size() - 2));
				} else {
					inst.setValue(ATTRIBUTES.get(ATTRIBUTES.size() - 2),
							datasetName);
				}

				// Debug output

				sb = new StringBuffer("[senti:");
				sb.append(classValue.get());
				sb.append(" aspect:");
				sb.append(aspectStr);
				sb.append(" ");
				sb.append(FeatureExtractor
						.countSentimentToString(sentimentCount));
				sb.append("] ");
				sb.append(featuresString);
				tagged.add(sb.toString());

				data.add(inst);

				count++;
			}

			save(tagged, outFilePath(parentDir, filename + "_tagged", "txt"));

		} finally {
			if (br != null) {
				br.close();
			}
		}
		System.out.printf("Features extracted. Processed %s rows\n", count);
		System.out.printf("%s distinct aspects: %s\n", aspects.size(), aspects);
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

	private static String outFilePath(String parentDir, String inFilename,
			String ext) {
		StringBuffer path = new StringBuffer(parentDir);

		path.append("\\");
		path.append(inFilename);
		path.append(".");
		path.append(ext);
		return path.toString();
	}

	private static void saveArff(Instances data, String filePath)
			throws IOException {
		ArffSaver arff = new ArffSaver();
		arff.setInstances(data);
		arff.setFile(new File(filePath));

		arff.writeBatch();
		System.out.printf("Saved %s attributes and %s instances:\n\t%s\n",
				data.numAttributes(), data.size(), filePath);

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
