package cs4242.a3;

import static cs4242.a3.PartOfSpeech.VOCABULARY_WHITELIST;

import java.util.Set;

import com.google.common.base.CharMatcher;

public final class Vocabulary {

	private static Set<String> vocab;
	
	//private static final Set<String> VOCABULARY = FileUtil.load(
		//	System.getProperty("a3.vocab.file.path"));

	private Vocabulary() {
		// Private constructor, not meant to be instantiated
	}
	
	public static void init(String filePath) {
		vocab =  FileUtil.load(filePath);
	}

	public static boolean has(String word) {
		return vocab.contains(word);
	}

	public static boolean valid(Word word) {

		String pos = word.pos();
		String nTerm = word.normalizedTerm();

		return VOCABULARY_WHITELIST.contains(pos)
				&& alphanumericWithAtLeastOneLetter(nTerm);

	}

	public static boolean alphanumericWithAtLeastOneLetter(String word) {

		// Only use Unicode letters and digits,
		// otherwise cannot be saved as ARFF

		// Alphanumeric
		if (CharMatcher.inRange('a', 'z').or(CharMatcher.inRange('0', '9'))
				.matchesAllOf(word)) {
			// Alphanumeric with at least one letter
			return CharMatcher.inRange('a', 'z').matchesAnyOf(word);
		}
		return false;
	}

	
}
