package cs4242.a2;

import static com.google.common.base.Preconditions.checkState;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.gson.Gson;

public final class FeatureExtractor {

	private FeatureExtractor() {
		// Private constructor, not meant to be instantiated
	}

	public static void main(String[] args) throws IOException {

		if (args.length != 2) {
			System.out.println("Usage: FeatureExtractor <train.csv> <tweets.json>");
			System.exit(1);
		}
		String trainPath = args[0];
		String tweetsPath = args[1];

		long startTime = System.currentTimeMillis();
		
		Map<String, TextFeatureVector> trainData = trainData(trainPath);
		
		Map<String, List<Tweet>> tweetsData = tweetsData(tweetsPath);
		
		// TODO print contents in tweets data
		
		long elapsedTime = System.currentTimeMillis() - startTime;
		System.out.printf("Done! Run time: %ss\n", elapsedTime / 1000);

	}
	
	public static Map<String, List<Tweet>> tweetsData(String tweetsFilePath) throws IOException {
		Map<String, List<Tweet>> result = new HashMap<String, List<Tweet>>();
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
				userId = tw.userId();
				if (result.containsKey(userId)) {
					tweets = result.get(userId);
					tweets.add(tw);
				} else {
					tweets = new ArrayList<Tweet>();
					tweets.add(tw);
					result.put(userId, tweets);
				}
				count++;
				
			}
		} finally {
			if (br != null) {
				br.close();
			}
		}
		System.out.printf("Loaded tweets data: %s users and %s tweets\n", result.size(), count);
		return result;
	}

	public static Map<String, TextFeatureVector> trainData(String trainFilePath)
			throws IOException {
		Map<String, TextFeatureVector> users = new HashMap<String, TextFeatureVector>();
		final CharMatcher WHITESPACE_DOUBLE_QUOTES = CharMatcher.WHITESPACE.or(CharMatcher.is('\"'));
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
				
				tokens = Splitter.on(",\"").trimResults(WHITESPACE_DOUBLE_QUOTES).splitToList(line);
				countTokens = tokens.size();
				checkState(countTokens == 3, "Expecting 3 tokens but found %s", countTokens);
				userId = tokens.get(0);
				gender = tokens.get(1);
				age = tokens.get(2);
				fv = new TextFeatureVector(userId, gender, age);
				users.put(userId, fv);
				
			}
		} finally {
			if (br != null) {
				br.close();
			}
		}
		System.out.printf("Loaded train data (%s users)\n", users.size());
		return users;
	}

	public static String normalize(String s) {
		s = Strings.nullToEmpty(s);
		s = s.toLowerCase();
		s = CharMatcher.WHITESPACE.trimFrom(s);
		return s;
	}

}
