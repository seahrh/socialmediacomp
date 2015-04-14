package cs4242.a3;

import static com.google.common.base.Preconditions.checkArgument;
import static cs4242.a3.PartOfSpeech.*;

import java.util.Set;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;



public class MpqaClue {

	public static final char POS_DELIMITER = '_';

	public static final String POSITIVE = "positive";
	public static final String NEGATIVE = "negative";
	public static final String POSITIVE_AND_NEGATIVE = "both";
	public static final String NEUTRAL = "neutral";
	private static final Set<String> SENTIMENT_POLARITY = Sets.newHashSet(
			POSITIVE, NEGATIVE, POSITIVE_AND_NEGATIVE, NEUTRAL);

	public static final String ANY_PART_OF_SPEECH = "anypos";
	private static final String ADJECTIVE = "adj";
	private static final String NOUN = "noun";
	private static final String ADVERB = "adverb";
	private static final String VERB = "verb";
	private static final Set<String> PARTS_OF_SPEECH = Sets.newHashSet(
			ANY_PART_OF_SPEECH, ADJECTIVE, NOUN, ADVERB, VERB);

	private boolean stronglySubjective;
	private String word;
	private String pos;
	private boolean stemmed;
	private String polarity;

	private MpqaClue() {
		stronglySubjective = false;
		word = null;
		pos = null;
		stemmed = false;
		polarity = null;
	}

	public MpqaClue(String word, String pos, String polarity,
			boolean stronglySubjective, boolean stemmed) {
		this();

		String val = Strings.nullToEmpty(word);
		val = CharMatcher.WHITESPACE.trimFrom(val).toLowerCase();
		checkArgument(!val.isEmpty(), "Word cannot be null or empty string");
		this.word = val;

		val = Strings.nullToEmpty(pos);
		val = CharMatcher.WHITESPACE.trimFrom(val).toLowerCase();
		checkArgument(!val.isEmpty(), "POS cannot be null or empty string");
		checkArgument(PARTS_OF_SPEECH.contains(val),
				"Invalid polarity value [%s]", pos);
		this.pos = val;

		val = Strings.nullToEmpty(polarity);
		val = CharMatcher.WHITESPACE.trimFrom(val).toLowerCase();
		checkArgument(!val.isEmpty(), "Polarity cannot be null or empty string");
		checkArgument(SENTIMENT_POLARITY.contains(val),
				"Invalid polarity value [%s]", polarity);
		this.polarity = val;

		this.stronglySubjective = stronglySubjective;
		this.stemmed = stemmed;
	}

	public boolean stronglySubjective() {
		return stronglySubjective;
	}

	public String word() {
		return word;
	}

	public String pos() {
		return pos;
	}

	public boolean stemmed() {
		return stemmed;
	}

	public boolean isPositive() {
		if (polarity.equals(POSITIVE)) {
			return true;
		}
		return false;
	}

	public boolean isNegative() {
		if (polarity.equals(NEGATIVE)) {
			return true;
		}
		return false;
	}

	public boolean isNeutral() {
		if (polarity.equals(NEUTRAL)) {
			return true;
		}
		return false;
	}

	public boolean isPositiveAndNegative() {
		if (polarity.equals(POSITIVE_AND_NEGATIVE)) {
			return true;
		}
		return false;
	}

	public String key() {

		return word;
	}

	public static String mpqaPos(String pennPos) {
		if (ADJECTIVE_POS.contains(pennPos)) {
			return ADJECTIVE;
		}
		if (VERB_POS.contains(pennPos)) {
			return VERB;
		}
		if (NOUN_POS.contains(pennPos)) {
			return NOUN;
		}
		if (ADVERB_POS.contains(pennPos)) {
			return ADVERB;
		}
		return "";
	}

	public boolean matches(String term, String pennPos) {

		if (term.equals(word)) {
			if (pos.equals(ANY_PART_OF_SPEECH)
					|| pos.equals(MpqaClue.mpqaPos(pennPos))) {
				return true;
			}

		}

		return false;
	}
	
	public Word setSentiment(Word word) {
		if (isPositive()) {
			word.positiveSentiment(true);
		} else if (isNegative()) {
			word.negativeSentiment(true);
		} else if (isPositiveAndNegative()) {
			word.positiveAndNegativeSentiment(true);
		} else if (isNeutral()) {
			word.neutralSentiment(true);
		}
		

		if (stronglySubjective()) {
			word.stronglySubjective(true);
		}
		return word;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((polarity == null) ? 0 : polarity.hashCode());
		result = prime * result + ((pos == null) ? 0 : pos.hashCode());
		result = prime * result + (stemmed ? 1231 : 1237);
		result = prime * result + (stronglySubjective ? 1231 : 1237);
		result = prime * result + ((word == null) ? 0 : word.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof MpqaClue)) {
			return false;
		}
		MpqaClue other = (MpqaClue) obj;
		if (polarity == null) {
			if (other.polarity != null) {
				return false;
			}
		} else if (!polarity.equals(other.polarity)) {
			return false;
		}
		if (pos == null) {
			if (other.pos != null) {
				return false;
			}
		} else if (!pos.equals(other.pos)) {
			return false;
		}
		if (stemmed != other.stemmed) {
			return false;
		}
		if (stronglySubjective != other.stronglySubjective) {
			return false;
		}
		if (word == null) {
			if (other.word != null) {
				return false;
			}
		} else if (!word.equals(other.word)) {
			return false;
		}
		return true;
	}
}
