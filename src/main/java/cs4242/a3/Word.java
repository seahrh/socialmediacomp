package cs4242.a3;

import static com.google.common.base.Preconditions.checkArgument;
import static cs4242.a3.StringUtil.*;
import static cs4242.a3.PartOfSpeech.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;

public class Word {

	

	private static final char SEPARATOR = '_';

	private static final CharMatcher NORMALIZE_HASHTAG = CharMatcher.anyOf(HEX
			.removeFrom(SYMBOL_CHARACTERS));

	private static final CharMatcher NORMALIZE_MENTION = CharMatcher
			.anyOf((AMPERSAND.or(UNDERSCORE).removeFrom(SYMBOL_CHARACTERS)));

	// Immutable fields

	private String term;
	private String pos;
	private String normalizedTerm;

	// Mutable fields

	private boolean negated;
	private boolean positiveSentiment;
	private boolean negativeSentiment;
	private boolean positiveAndNegativeSentiment;
	private boolean neutralSentiment;
	private boolean stronglySubjective;

	private Word() {
		term = null;
		negated = false;
		pos = null;
		normalizedTerm = null;
		positiveSentiment = false;
		negativeSentiment = false;
		positiveAndNegativeSentiment = false;
		neutralSentiment = false;
		stronglySubjective = false;
	}

	public Word(String term, String pos) {
		this();
		term(term);
		pos(pos);
		setNormalizedTerm();
	}

	public Word(String taggedWord) {
		this();
		int separatorIndex = taggedWord.lastIndexOf(String.valueOf(SEPARATOR));
		checkArgument(separatorIndex != -1,
				"Tagged word must have POS component. [%s]", taggedWord);
		String term = taggedWord.substring(0, separatorIndex);
		String pos = taggedWord.substring(separatorIndex + 1);
		term(term);
		pos(pos);
		setNormalizedTerm();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (negated) {
			sb.append("NOT");
			sb.append(SEPARATOR);
		}

		sb.append(normalizedTerm);
		sb.append(SEPARATOR);
		sb.append(pos);
		return sb.toString();
	}

	/**
	 * @return the term
	 */
	public String term() {
		return term;
	}

	private void term(String term) {
		String val = trim(term);
		checkArgument(!val.isEmpty(), "Term cannot be null or empty string");
		val = CONTROL_CHARACTERS.removeFrom(val);
		this.term = val.toLowerCase();

	}

	/**
	 * @return the term
	 */
	public String normalizedTerm() {
		return normalizedTerm;
	}

	/**
	 * Normalized term should only be set after the unnormalized term and pos
	 * are set
	 */
	private void setNormalizedTerm() {
		String val = term;
		if (TERM_NORMALIZATION_WHITELIST.contains(pos)) {
			val = normalize(term, pos);
		}
		normalizedTerm = val;
	}

	/**
	 * @return the negated
	 */
	public boolean negated() {
		return negated;
	}

	/**
	 * @param negated
	 *            the negated to set
	 */
	public void negated(boolean negated) {
		if (!negated) {
			this.negated = false;
			return;
		}
		
		if (negated && NEGATION_WHITELIST.contains(pos)) {
			this.negated = true;
		}
		
	}

	/**
	 * @return the pos
	 */
	public String pos() {
		return pos;
	}

	private void pos(String pos) {
		String val = trim(pos);
		checkArgument(!val.isEmpty(), "POS cannot be null or empty string");
		this.pos = val.toUpperCase();
	}

	/**
	 * @return the positiveSentiment
	 */
	public boolean positiveSentiment() {
		return positiveSentiment;
	}

	/**
	 * @param positiveSentiment
	 *            the positiveSentiment to set
	 */
	public void positiveSentiment(boolean positiveSentiment) {
		this.positiveSentiment = positiveSentiment;
	}

	/**
	 * @return the negativeSentiment
	 */
	public boolean negativeSentiment() {
		return negativeSentiment;
	}

	/**
	 * @param negativeSentiment
	 *            the negativeSentiment to set
	 */
	public void negativeSentiment(boolean negativeSentiment) {
		this.negativeSentiment = negativeSentiment;
	}

	/**
	 * @return the positiveAndNegativeSentiment
	 */
	public boolean positiveAndNegativeSentiment() {
		return positiveAndNegativeSentiment;
	}

	/**
	 * @param positiveAndNegativeSentiment
	 *            the positiveAndNegativeSentiment to set
	 */
	public void positiveAndNegativeSentiment(
			boolean positiveAndNegativeSentiment) {
		this.positiveAndNegativeSentiment = positiveAndNegativeSentiment;
	}

	/**
	 * @return the neutralSentiment
	 */
	public boolean neutralSentiment() {
		return neutralSentiment;
	}

	/**
	 * @param neutralSentiment
	 *            the neutralSentiment to set
	 */
	public void neutralSentiment(boolean neutralSentiment) {
		this.neutralSentiment = neutralSentiment;
	}

	/**
	 * @return the stronglySubjective
	 */
	public boolean stronglySubjective() {
		return stronglySubjective;
	}

	/**
	 * @param stronglySubjective
	 *            the stronglySubjective to set
	 */
	public void stronglySubjective(boolean stronglySubjective) {
		this.stronglySubjective = stronglySubjective;
	}

	public boolean hasLetter() {
		return CharMatcher.JAVA_LETTER.matchesAnyOf(term);
	}

	public static String concat(List<Word> features, char separator) {
		List<String> result = new ArrayList<String>();
		for (Word f : features) {
			result.add(f.toString());
		}
		return Joiner.on(separator).join(result);
	}

	public static String normalize(String term, String pos) {

		// Remove all punctuation, symbols

		String val = PUNCTUATION.removeFrom(term);

		if (pos.equals("HT")) {
			val = NORMALIZE_HASHTAG.removeFrom(val);
		} else if (pos.equals("USR")) {
			val = NORMALIZE_MENTION.removeFrom(val);
		} else {
			val = SYMBOLS.removeFrom(val);
		}

		if (!val.isEmpty()) {
			val = val.toLowerCase();
			val = PartOfSpeech.lemmatize(val, pos);
		}
		return val;
	}
	
	

}
