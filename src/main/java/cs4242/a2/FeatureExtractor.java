package cs4242.a2;

import static com.google.common.base.Preconditions.checkState;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;

public final class FeatureExtractor {

	private FeatureExtractor() {
		// Private constructor, not meant to be instantiated
	}

	public static void main(String[] args) throws IOException {

		if (args.length != 1) {
			System.out.println("Usage: FeatureExtractor <train.csv>");
			System.exit(1);
		}
		String trainPath = args[0];

		long startTime = System.currentTimeMillis();
		
		Map<String, TextFeatureVector> trainData = trainFile(trainPath);
		
		long elapsedTime = System.currentTimeMillis() - startTime;
		System.out.printf("Done! Run time: %ss\n", elapsedTime / 1000);

	}

	public static Map<String, TextFeatureVector> trainFile(String path)
			throws IOException {
		Map<String, TextFeatureVector> users = new HashMap<String, TextFeatureVector>();
		final CharMatcher WHITESPACE_DOUBLE_QUOTES = CharMatcher.WHITESPACE.or(CharMatcher.is('\"'));
		BufferedReader br = null;
		String line = "";
		
		int countTokens = 0;
		File file = new File(path);
		List<String> tokens;
		TextFeatureVector fv;
		String userId;
		String gender;
		String age;
		System.out.printf("Loading train data...\n\t%s\n", path);
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
