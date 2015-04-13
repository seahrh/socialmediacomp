package cs4242.a3;

import static cs4242.a3.StringUtil.PUNCTUATION;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

public final class Negation {

	private static final Set<String> NEGATION_WORDS = Sets.newHashSet("aren't",
			"can't", "couldn't", "cannot", "didn't", "doesn't", "don't",
			"hadn't", "hasn't", "haven't", "isn't", "mustn't", "no", "nor",
			"not", "shan't", "shouldn't", "wasn't", "weren't", "won't",
			"wouldn't", "neither", "never", "nowhere", "hardly", "scarcely",
			"barely");
	
	private static final Set<String> NEGATION_TERMINATORS = Sets.newHashSet(
			"but", "however");

	private Negation() {
		// Private constructor, not meant to be instantiated
	}

	public static List<Word> detect(List<Word> words) {
		List<Word> result = new ArrayList<Word>(words.size());
		boolean negation = false;
		String term;
		String normalizedTerm;
		

		for (Word word : words) {
			term = word.term();
			
			// Normalized term does not contain punctuation
			
			normalizedTerm = word.normalizedTerm();
			
			
			if (negation) {
				if (PUNCTUATION.matchesAnyOf(term)
						|| NEGATION_TERMINATORS.contains(normalizedTerm)) {
					negation = false;
				} else {
					word.negated(true);
				}
			} else {
				if (NEGATION_WORDS.contains(normalizedTerm)) {
					negation = true;
				}
			}
			result.add(word);
		}

		return result;
	}
}
