package cs4242.a3;

import static cs4242.a3.PartOfSpeech.VOCABULARY_WHITELIST;

import java.util.Set;

import com.google.common.base.CharMatcher;
import com.google.common.collect.Sets;

public final class Vocabulary {

	private static final Set<String> VOCABULARY = Sets.newHashSet();

	private Vocabulary() {
		// Private constructor, not meant to be instantiated
	}

	public static boolean has(String word) {
		return VOCABULARY.contains(word);
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
