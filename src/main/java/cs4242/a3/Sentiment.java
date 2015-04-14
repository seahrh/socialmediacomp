package cs4242.a3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;

import cs4242.a3.MpqaClue;

public final class Sentiment {

	private static final Map<String, Set<MpqaClue>> MPQA_LEXICON = mpqa();

	private Sentiment() {
		// Private constructor, not meant to be instantiated
	}

	public static List<Word> detect(List<Word> words) {
		List<Word> result = new ArrayList<Word>();
		Set<MpqaClue> clues;
		String normalizedTerm;
		String pos;
		MpqaClue anyClue;
		boolean matched = false;

		for (Word word : words) {
			normalizedTerm = word.normalizedTerm();
			pos = word.pos();

			clues = MPQA_LEXICON.get(normalizedTerm);

			if (clues != null && clues.size() > 0) {
				for (MpqaClue clue : clues) {

					// Find best matching clue using term and POS

					if (clue.matches(normalizedTerm, pos)) {
						word = clue.setSentiment(word);
						matched = true;
						break;
					}
				}

				// Cannot get a best match
				// Just use any rule from the set

				if (!matched) {
					anyClue = clues.iterator().next();
					word = anyClue.setSentiment(word);
				}

			}
			result.add(word);
		}

		return result;
	}

	public static Map<String, Double> countSentiment(List<Word> words) {
		Map<String, Double> result = new HashMap<String, Double>();
		result.put("strong_positive", 0d);
		result.put("strong_negative", 0d);
		result.put("strong_neutral", 0d);
		//result.put("strong_posneg", 0d);
		result.put("weak_positive", 0d);
		result.put("weak_negative", 0d);
		result.put("weak_neutral", 0d);
		//result.put("weak_posneg", 0d);
		boolean negated = false;
		double count = 0;

		for (Word word : words) {

			negated = word.negated();

			// If word carries negation,
			// reverse sentiment polarity for positive,negative
			// (ignore negation for neutral and pos-neg)
			// and set as weak subjective

			if (word.stronglySubjective()) {

				if (word.positiveSentiment()) {

					if (!negated) {
						count = result.get("strong_positive") + 1;
						result.put("strong_positive", count);
					} else {
						count = result.get("weak_negative") + 1;
						result.put("weak_negative", count);
					}
				}

				if (word.negativeSentiment()) {
					if (!negated) {
						count = result.get("strong_negative") + 1;
						result.put("strong_negative", count);
					} else {
						count = result.get("weak_positive") + 1;
						result.put("weak_positive", count);
					}
				}

				if (word.neutralSentiment()) {
					count = result.get("strong_neutral") + 1;
					result.put("strong_neutral", count);
				}

				if (word.positiveAndNegativeSentiment()) {
					//count = result.get("strong_posneg") + 1;
					//result.put("strong_posneg", count);
				}

			} else {

				// Weakly subjective

				if (word.positiveSentiment()) {
					if (!negated) {
						count = result.get("weak_positive") + 1;
						result.put("weak_positive", count);
					} else {
						count = result.get("weak_negative") + 1;
						result.put("weak_negative", count);
					}
				}

				if (word.negativeSentiment()) {
					if (!negated) {
						count = result.get("weak_negative") + 1;
						result.put("weak_negative", count);
					} else {
						count = result.get("weak_positive") + 1;
						result.put("weak_positive", count);
					}
				}

				if (word.neutralSentiment()) {
					count = result.get("weak_neutral") + 1;
					result.put("weak_neutral", count);
				}

				if (word.positiveAndNegativeSentiment()) {
					//count = result.get("weak_posneg") + 1;
					//result.put("weak_posneg", count);
				}
			}
		}

		return result;
	}

	private static Map<String, Set<MpqaClue>> mpqa() {
		String filePath = System.getProperty("a3.sentiment.mpqa.file.path");
		Map<String, Set<MpqaClue>> lookup = new HashMap<String, Set<MpqaClue>>();
		Set<MpqaClue> clues;
		File file = new File(filePath);
		BufferedReader br = null;
		String line;
		int count = 0;
		MpqaClue clue;
		boolean stronglySubjective = false;
		String word = null;
		String pos = null;
		boolean stemmed = false;
		String polarity = null;
		List<String> tokens;
		String key;
		Set<String> allPos = new HashSet<String>();

		final String TYPE = "type=";
		final String WORD = "word1=";
		final String POS = "pos1=";
		final String STEMMED = "stemmed1=";
		final String POLARITY = "priorpolarity=";

		try {
			System.out.printf("Loading MPQA clues...\n\t%s\n", filePath);
			br = new BufferedReader(new FileReader(file));
			while ((line = br.readLine()) != null) {
				tokens = Splitter.on(CharMatcher.WHITESPACE).trimResults()
						.splitToList(line);

				for (String token : tokens) {
					if (token.startsWith(TYPE)) {
						token = token.substring(TYPE.length());
						if (token.equals("strongsubj")) {
							stronglySubjective = true;
						} else {
							stronglySubjective = false;
						}
					} else if (token.startsWith(WORD)) {
						word = token.substring(WORD.length());

					} else if (token.startsWith(POS)) {
						pos = token.substring(POS.length());
						allPos.add(pos);

					} else if (token.startsWith(STEMMED)) {
						token = token.substring(STEMMED.length());
						if (token.equals("y")) {
							stemmed = true;
						} else {
							stemmed = false;
						}

					} else if (token.startsWith(POLARITY)) {
						polarity = token.substring(POLARITY.length());

					}
				}

				clue = new MpqaClue(word, pos, polarity, stronglySubjective,
						stemmed);
				key = clue.key();

				if (lookup.containsKey(key)) {
					clues = lookup.get(key);

				} else {
					clues = new HashSet<MpqaClue>();

				}
				clues.add(clue);
				lookup.put(key, clues);
				count++;
			}

			System.out
					.printf("MPQA file has %s lines. Loaded clues for %s distinct words\n",
							count, lookup.size());

			System.out.printf("MPQA parts of speech: %s\n", allPos);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException(e.getMessage(), e);
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new IllegalStateException(e.getMessage(), e);
			}
		}
		return lookup;
	}

}
