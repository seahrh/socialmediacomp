package cs4242.a1;

import static com.google.common.base.Preconditions.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
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
	
	private static final Set<String> MPQA_ADJECTIVE_POS = Sets.newHashSet("JJ",
			"JJR", "JJS");

	private static final Set<String> MPQA_VERB_POS = Sets.newHashSet("VB",
			"VBD", "VBG", "VBN", "VBP", "VBZ");

	private static final Set<String> MPQA_NOUN_POS = Sets.newHashSet("NN",
			"NNP", "NNPS", "NNS");

	private static final Set<String> MPQA_ADVERB_POS = Sets.newHashSet("RB",
			"RBR", "RBS");

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

	public String polarity() {
		return polarity;
	}

	public String key() {
		// StringBuffer result = new StringBuffer(word);
		// result.append("_");
		// result.append(pos);
		// return result.toString();
		return word;
	}

	public static Map<String, Set<MpqaClue>> load(String filePath)
			throws IOException {
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

			System.out.printf("MPQA file has %s lines. Loaded clues for %s distinct words\n",
					count, lookup.size());

			System.out.printf("MPQA parts of speech: %s\n", allPos);
		} finally {
			if (br != null) {
				br.close();
			}
		}
		return lookup;
	}

	public static String mpqaPos(String pennPos) {
		if (MPQA_ADJECTIVE_POS.contains(pennPos)) {
			return ADJECTIVE;
		}
		if (MPQA_VERB_POS.contains(pennPos)) {
			return VERB;
		}
		if (MPQA_NOUN_POS.contains(pennPos)) {
			return NOUN;
		}
		if (MPQA_ADVERB_POS.contains(pennPos)) {
			return ADVERB;
		}
		return "";
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
