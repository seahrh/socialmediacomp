package cs4242.a2;

import static com.google.common.base.Preconditions.checkState;
import static cs4242.a2.FileUtil.save;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.champeau.ld.UberLanguageDetector;

import org.languagetool.JLanguageTool;
import org.languagetool.language.English;
import org.languagetool.rules.RuleMatch;

import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.primitives.Ints;
import com.google.gson.Gson;

public final class FeatureExtractor {

	private static List<String> userIds;
	private static Map<String, TextFeatureVector> trainData;
	private static Map<String, List<Tweet>> tweetsData;

	private FeatureExtractor() {
		// Private constructor, not meant to be instantiated
	}

	public static void main(String[] args) {

		if (args.length != 7) {
			System.out
					.println("Usage: FeatureExtractor <train.csv> <tweets.json> <tweets output directory> <print tweets flag> <train set output directory>");
			System.exit(1);
		}
		String trainPath = args[0];
		String tweetsPath = args[1];
		String tweetsOutDir = args[2];
		String printTweetsFlag = args[3];
		String workingDir = args[4];
		String liwcPath = args[5];
		String runSpellChecker = args[6];

		long startTime = System.currentTimeMillis();

		try {

			trainData(trainPath);

			tweetsData(tweetsPath);

			if (printTweetsFlag.equals("printTweets:true")) {
				printTweets(tweetsData, tweetsOutDir);
			}

			liwcData(liwcPath);

			if (runSpellChecker.equals("runSpellChecker:true")) {
				checkSpelling(trainData, tweetsData);
				saveSpellingResult(trainData, workingDir);
			} else {
				loadSpellingResult(trainData, workingDir);
			}

			saveTrainSet(workingDir);

		} catch (Exception e) {
			e.printStackTrace();
		}

		long elapsedTime = System.currentTimeMillis() - startTime;
		System.out.printf("Done! Run time: %ss\n", elapsedTime / 1000);

	}

	public static void detectLang(Map<String, List<Tweet>> tweets) {
		UberLanguageDetector detector = UberLanguageDetector.getInstance();
		String text = "";
		String lang = "";
		List<Tweet> tws = null;
		for (Map.Entry<String, List<Tweet>> entry : tweets.entrySet()) {
			tws = entry.getValue();
			for (Tweet tw : tws) {
				text = tw.text();
				lang = detector.detectLang(text);
				if (!lang.equals("en")) {
					System.out.printf("%s: %s\n", lang, text);
				}
			}
		}
	}

	public static Map<String, TextFeatureVector> checkSpelling(
			Map<String, TextFeatureVector> features,
			Map<String, List<Tweet>> tweets) throws IOException {
		JLanguageTool langTool = new JLanguageTool(new English());
		langTool.activateDefaultPatternRules();
		// langTool.setListUnknownWords(true);
		List<RuleMatch> matches = null;
		String userId = "";
		TextFeatureVector fv = null;
		int errors = 0;
		List<Tweet> tws;
		String text = "";
		System.out.print("Check spelling... ");

		for (Map.Entry<String, TextFeatureVector> entry : features.entrySet()) {
			userId = entry.getKey();
			errors = 0;
			tws = tweets.get(userId);

			if (tws != null) {
				for (Tweet tw : tws) {
					text = tw.text();
					matches = langTool.check(text);

					errors += matches.size();

				}
			}

			fv = entry.getValue();
			fv.spellingErrors(errors);
		}
		System.out.println("Done!");
		return features;
	}

	private static void saveSpellingResult(
			Map<String, TextFeatureVector> features, String outDir)
			throws IOException {
		String out = "";
		final char SEPARATOR = ',';
		String userId = "";
		StringBuilder sb = new StringBuilder(outDir);
		sb.append(File.separator);
		sb.append("spell.txt");
		String path = sb.toString();

		TextFeatureVector fv = null;
		sb = new StringBuilder();
		for (Map.Entry<String, TextFeatureVector> entry : features.entrySet()) {
			userId = entry.getKey();
			fv = entry.getValue();
			sb.append(userId);
			sb.append(SEPARATOR);
			sb.append(fv.spellingErrors());
			sb.append("\n");

		}
		out = sb.toString();
		save(out, path);
	}

	private static Map<String, TextFeatureVector> loadSpellingResult(
			Map<String, TextFeatureVector> features, String dirPath)
			throws IOException {

		StringBuilder sb = new StringBuilder(dirPath);
		sb.append(File.separator);
		sb.append("spell.txt");
		String path = sb.toString();
		String userId = "";
		File file = new File(path);
		BufferedReader br = null;
		String line = "";
		TextFeatureVector fv = null;
		List<String> values = null;
		int numValues = 0;

		System.out.printf("Loading spelling results...\n\t%s\n", path);
		try {

			br = new BufferedReader(new FileReader(file));

			while ((line = br.readLine()) != null) {

				values = Splitter.on(",").trimResults().splitToList(line);
				numValues = values.size();
				checkState(numValues == 2, "Expected 2 values but found %s",
						numValues);

				userId = values.get(0);

				fv = features.get(userId);

				if (fv != null) {
					fv.spellingErrors(Ints.tryParse(values.get(1)));
				}

			}
		} finally {
			if (br != null) {
				br.close();
			}
		}
		System.out.println("Loaded spelling results");

		return features;
	}

	public static void saveTrainSet(String outDir) throws Exception {

		StringBuilder sb = new StringBuilder(outDir);
		sb.append(File.separator);

		String path = sb.append("text_train.arff").toString();
		String relationName = "Text features";

		ArrayList<Attribute> attrs = TextFeatureVector.baseHeader(userIds);
		attrs.addAll(TextFeatureVector.liwcHeader());
		attrs.addAll(TextFeatureVector.spellHeader());

		Instances data = new Instances(relationName, attrs, trainData.size());

		TextFeatureVector fv = null;

		for (Map.Entry<String, TextFeatureVector> entry : trainData.entrySet()) {
			fv = entry.getValue();
			data.add(fv.getInstance(data));
		}

		// data.setClass(data.attribute("gender"));
		saveArff(data, path);

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

	public static void printTweets(Map<String, List<Tweet>> tweetsData,
			String outDir) throws IOException {

		File file = null;
		BufferedWriter bw = null;
		String userId = "";
		String path = "";
		StringBuilder sb;
		String line = "";

		if (!outDir.endsWith(File.separator)) {
			outDir += File.separator;
		}

		for (Map.Entry<String, List<Tweet>> entry : tweetsData.entrySet()) {
			userId = entry.getKey();
			sb = new StringBuilder(outDir);
			path = sb.append(userId).append(".txt").toString();
			file = new File(path);
			try {

				bw = new BufferedWriter(new FileWriter(file));
				for (Tweet tw : entry.getValue()) {
					sb = new StringBuilder(tw.text());
					sb.append("\n");
					line = sb.toString();
					bw.write(line);
				}

			} finally {
				if (bw != null) {
					bw.close();
				}
			}
		}
		System.out.printf("Printed tweets to directory:\n\t%s\n", outDir);
	}

	public static Map<String, List<Tweet>> tweetsData(String tweetsFilePath)
			throws IOException {
		tweetsData = new HashMap<String, List<Tweet>>();
		BufferedReader br = null;
		String line = "";
		File file = new File(tweetsFilePath);
		Gson gson = new Gson();
		List<Tweet> tweets;
		Tweet tw;
		String userId;
		int count = 0;
		System.out.printf("Loading tweets data...\n\t%s\n", tweetsFilePath);
		try {

			br = new BufferedReader(new FileReader(file));

			while ((line = br.readLine()) != null) {

				tw = gson.fromJson(line, Tweet.class);

				// Strip control characters

				tw.text(tw.text());
				userId = tw.userId();

				if (tweetsData.containsKey(userId)) {
					tweets = tweetsData.get(userId);
					tweets.add(tw);
				} else {
					tweets = new ArrayList<Tweet>();
					tweets.add(tw);
					tweetsData.put(userId, tweets);
				}
				count++;

			}
		} finally {
			if (br != null) {
				br.close();
			}
		}
		System.out.printf("Loaded tweets data: %s users and %s tweets\n",
				tweetsData.size(), count);
		return tweetsData;
	}

	public static Map<String, TextFeatureVector> trainData(String trainFilePath)
			throws IOException {
		trainData = new HashMap<String, TextFeatureVector>();
		userIds = new ArrayList<String>();
		final CharMatcher WHITESPACE_DOUBLE_QUOTES = CharMatcher.WHITESPACE
				.or(CharMatcher.is('\"'));
		BufferedReader br = null;
		String line = "";

		int countTokens = 0;
		File file = new File(trainFilePath);
		List<String> tokens;
		TextFeatureVector fv;
		String userId;
		String gender;
		String age;

		System.out.printf("Loading train data...\n\t%s\n", trainFilePath);
		try {

			br = new BufferedReader(new FileReader(file));

			// Skip the header row

			br.readLine();

			while ((line = br.readLine()) != null) {

				tokens = Splitter.on(",\"")
						.trimResults(WHITESPACE_DOUBLE_QUOTES)
						.splitToList(line);
				countTokens = tokens.size();
				checkState(countTokens == 3, "Expecting 3 tokens but found %s",
						countTokens);
				userId = tokens.get(0);
				gender = tokens.get(1);
				age = tokens.get(2);
				fv = new TextFeatureVector(userId, gender, age);
				trainData.put(userId, fv);
				userIds.add(userId);
			}
		} finally {
			if (br != null) {
				br.close();
			}
		}
		System.out.printf("Loaded train data (%s users)\n", trainData.size());

		return trainData;
	}

	public static Map<String, TextFeatureVector> liwcData(String filePath)
			throws IOException {
		BufferedReader br = null;
		String line = "";
		File file = new File(filePath);
		List<String> values = null;
		String userId = "";
		TextFeatureVector fv = null;
		System.out.printf("Loading liwc data...\n\t%s\n", filePath);
		try {

			br = new BufferedReader(new FileReader(file));

			// Skip the header row

			br.readLine();

			while ((line = br.readLine()) != null) {
				values = Splitter.on(".txt\t").trimResults().splitToList(line);
				userId = values.get(0);

				fv = trainData.get(userId);

				if (fv != null) {
					fv.liwc(line);
				}

			}
		} finally {
			if (br != null) {
				br.close();
			}
		}
		System.out.println("Loaded liwc data");
		return trainData;
	}

	public static void debug(Map<String, List<Tweet>> tweetsData) {
		String userId = "";

		for (Map.Entry<String, List<Tweet>> entry : tweetsData.entrySet()) {
			userId = entry.getKey();
			for (Tweet tw : entry.getValue()) {
				System.out.printf("Userid: %s\t%s\n", userId, tw.text());
			}
		}
	}

}
