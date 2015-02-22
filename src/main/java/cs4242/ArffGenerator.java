package cs4242;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class FeatureExtractor {

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
		ATTRIBUTES.add(new Attribute("classlabel", values));

		// Text attribute is a string

		values = null;
		ATTRIBUTES.add(new Attribute("text", values));
	}

	public static void main(String[] args) throws IOException {

		if (args.length != 6) {
			System.out
					.println("Usage: FeatureExtractor <input> <stopwords> <output model> <tagger model> <tagged output> <lexicon>");
			System.exit(1);
		}

		String inFilePath = args[0];
		String inFilename = Files.getNameWithoutExtension(inFilePath);
		File inFile = new File(inFilePath);

		String stopwordsFilePath = args[1];
		String modelFilePath = args[2];
		String taggerPath = args[3];
		String taggedOutFilePath = args[4];
		String lexiconFilePath = args[5];

		String parentDir = inFile.getParent();
		BufferedReader br = null;
		String line;
		String features;
		String sentiment;
		Optional<String> classValue;
		List<String> row;
		int count = 0;
		Instances data = initDataset(inFilename);
		Instance inst;
		MaxentTagger tagger;
		List<String> taggedTweets = new ArrayList<String>();

		try {
			
			final Map<String, Set<MpqaClue>> MPQA_LEXICON = MpqaClue.cluesFromFile(lexiconFilePath);

			//System.out.println("Loading tagger...");
			//tagger = new MaxentTagger(taggerPath);
			//System.out.println("Tagger loaded successfully. Tagging tweets...");

			br = new BufferedReader(new FileReader(inFile));

			// Skip the header row

			br.readLine();

			while ((line = br.readLine()) != null) {
				inst = new DenseInstance(2);
				inst.setDataset(data);
				row = Splitter.on('\t').trimResults()
						.splitToList(line);

				sentiment = row.get(INPUT_FIELDS.indexOf("sentiment"));

				classValue = classValue(sentiment);

				if (classValue.isPresent()) {

					inst.setClassValue(classValue.get());
				} else {
					continue;
				}

				// TODO Normalize the string before tokenizing

				//features = extract(row.get(INPUT_FIELDS.indexOf("content")),
						//tagger);

				//taggedTweets.add(features);

				inst.setValue(ATTRIBUTES.get(1),
						row.get(INPUT_FIELDS.indexOf("content")));
				data.add(inst);

				count++;
			}

			System.out.printf("Processed %s rows\n", count);

			saveTaggedTweets(taggedTweets, taggedOutFilePath);

			// data = stringToWordVector(data, stopwordsFilePath);

			// saveArff(data, outFilePath(parentDir, inFilename));
			// System.out.printf("Saved %s attributes and %s instances\n",
			// data.numAttributes(), data.size());

			// svm(data, modelFilePath);

			// loadModel(modelFilePath);

		} catch (Exception e) {

			e.printStackTrace();
		} finally {
			br.close();
			System.out.println("Done!");
		}

	}

	private static void saveTaggedTweets(List<String> tweets, String path)
			throws IOException {
		File outFile = new File(path);
		BufferedWriter bw = null;

		try {
			bw = new BufferedWriter(new FileWriter(outFile));
			for (String tweet : tweets) {
				bw.write(tweet + "\n");
			}
		} finally {
			if (bw != null) {
				bw.close();
			}
		}
	}

	private static String extract(String tweet, MaxentTagger tagger) {
		String features = "";

		// Normalize to lowercase

		String val = Strings.nullToEmpty(tweet).toLowerCase();

		// Trim whitespace and single/double quotes enclosing the tweet
		// so that RT can be correctly tagged as retweet instead of NNP

		val = CharMatcher.anyOf("'\"").or(CharMatcher.WHITESPACE).trimFrom(val);

		if (val.length() != 0) {

			features = tagger.tagString(val);

		}
		return features;
	}

	/**
	 * Remove tags irrelevant to sentiment analysis.
	 * 
	 * @param s
	 * @return
	 */
	private static String normalize(String s) {
		String val = Strings.nullToEmpty(s);
		String feature;
		String term;
		String pos;
		int delimiterIndex = 0;
		StringBuffer normalized;
		List<String> features = Splitter.on(' ').trimResults()
				.omitEmptyStrings().splitToList(val);

		for (int i = 0; i < features.size(); i++) {
			feature = features.get(i);

			// Split into term and POS tag

			delimiterIndex = feature.lastIndexOf('_');
			term = feature.substring(0, delimiterIndex);
			pos = feature.substring(delimiterIndex);

			// Remove POS irrelevant to sentiment analysis

			/*
			 * if (pos.equals("_RT") || pos.equals("_:") || ) {
			 * features.remove(i); continue; }
			 */

			// TODO Normalize term
			normalized = new StringBuffer(term);
			normalized.append("_");
			normalized.append(pos);
			features.set(i, normalized.toString());
		}

		return Joiner.on(" ").join(features);
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

		// TODO should we still use stopwords?
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

		// tokenizer.setDelimiters(" \r\n\t.,;:");
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
		// svm.setModelFile(new File(modelPath));
		svm.buildClassifier(data);
		SerializationHelper.write(modelPath, svm);
		System.out.printf("SVM classifier trained\nModel file saved: %s\n",
				modelPath);
	}

	private static LibSVM loadModel(String path) throws Exception {
		LibSVM svm = (LibSVM) SerializationHelper.read(path);
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
