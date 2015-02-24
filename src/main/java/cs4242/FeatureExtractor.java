package cs4242;

import static com.google.common.base.Preconditions.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class FeatureExtractor {

	protected static final String PRUNED_POS_FILE = "C:\\Feng\\cs4242\\prunedpos.txt";
	protected static final String TAGGED_POS_FILE = "C:\\Feng\\cs4242\\tagged.txt";

	// Retain cardinal numbers such as 'million'
	// TODO Modifiers in . such as '!!'
	// TODO Whitelist is safer than blacklist

	private static final Set<String> POS_EXCLUSION_LIST = Sets.newHashSet("RT",
			"CC", "DT", "EX", "IN", "LS", "PDT", "POS", "PRP", "PRP$", "SYM",
			"TO", "WDT", "WP", "WP$", "WRB");

	private static final Set<String> PUNCTUATION_POS = Sets.newHashSet("``",
			"''", "(", ")", ",", "--", ".", ":");

	private static final Set<String> NEGATION_TERMINATORS = Sets.newHashSet(
			"but", "however");

	// Mark punctuation marks in preprocessing.
	// Punctuation marks exclude the following:
	// Hyphen (-) because don't want to break up words like 'anti-hero'
	// Single quote/apostrophe (') because don't want to break up words
	// like 'don't'

	private static final char[] PUNCTUATION_TO_DETECT = "[](){}:,`.!?\";\\/"
			.toCharArray();

	private static final String PUNCTUATION_MARKS = new String(
			PUNCTUATION_TO_DETECT) + "-'";

	// TODO Symbols like less-than (<), greater-than (>) may be useful...

	private static final String NOISE_CHARACTERS = "><*%&=^~|";

	private static final Set<String> TRAILING_NOISE = Sets.newHashSet("\\n",
			"'s", "s'");

	public static final int STRONG_POSITIVE_INDEX = 0;
	public static final int WEAK_POSITIVE_INDEX = 1;
	public static final int STRONG_NEGATIVE_INDEX = 2;
	public static final int WEAK_NEGATIVE_INDEX = 3;
	public static final int STRONG_NEUTRAL_INDEX = 4;
	public static final int WEAK_NEUTRAL_INDEX = 5;
	public static final int STRONG_POSNEG_INDEX = 6;
	public static final int WEAK_POSNEG_INDEX = 7;

	private Map<String, Set<MpqaClue>> mpqa;
	private MaxentTagger tagger;
	private Set<String> pruned;
	private Set<String> negationWords;

	private FeatureExtractor() {
		pruned = new HashSet<String>();
		mpqa = null;
		tagger = null;
		negationWords = null;
	}

	public FeatureExtractor(String taggerPath, String lexiconPath,
			String negationPath) throws IOException {
		this();

		mpqa = MpqaClue.load(lexiconPath);

		System.out.println("Loading POS tagger...");
		tagger = new MaxentTagger(taggerPath);
		System.out.println("Tagger loaded successfully.");

		negationWords(negationPath);
	}

	public List<Feature> extract(String text) {
		return pipeline(text);
	}

	private List<Feature> pipeline(String text) {

		String intermediate = preprocess(text);

		List<Feature> features = tagPos(intermediate);

		// Detect negation with full context of POS and punctuation

		features = detectNegation(features);

		// Discard unwanted POS, punctuation and characters

		features = prunePos(features, POS_EXCLUSION_LIST);

		features = prunePos(features, PUNCTUATION_POS);

		features = Feature.trimTerms(features, PUNCTUATION_MARKS);

		features = Feature.trimTermsTrailing(features, TRAILING_NOISE);

		// Detect sentiment with MPQA clue lookup by word
		// Hence terms must be clean (free of punctuation)
		// before this step.

		features = detectSentiment(features);

		return features;
	}

	public static int[] countSentiment(List<Feature> features) {
		int[] count = new int[8];
		boolean negated;

		for (Feature f : features) {
			negated = f.negated();

			// If feature is negated, ignore sentiment
			// Do not reverse polarity

			if (negated) {
				continue;
			}

			if (f.stronglySubjective()) {

				if (f.positiveSentiment()) {
					count[STRONG_POSITIVE_INDEX]++;
				}

				if (f.negativeSentiment()) {
					count[STRONG_NEGATIVE_INDEX]++;
				}

				if (f.neutralSentiment()) {
					count[STRONG_NEUTRAL_INDEX]++;
				}

				if (f.positiveAndNegativeSentiment()) {
					count[STRONG_POSNEG_INDEX]++;
				}

			} else {

				// Weakly subjective

				if (f.positiveSentiment()) {
					count[WEAK_POSITIVE_INDEX]++;
				}

				if (f.negativeSentiment()) {
					count[WEAK_NEGATIVE_INDEX]++;
				}

				if (f.neutralSentiment()) {
					count[WEAK_NEUTRAL_INDEX]++;
				}

				if (f.positiveAndNegativeSentiment()) {
					count[WEAK_POSNEG_INDEX]++;
				}
			}
		}

		return count;
	}

	public static String countSentimentToString(int[] count) {
		StringBuffer sb = new StringBuffer("ps:");
		sb.append(count[STRONG_POSITIVE_INDEX]);
		sb.append(" pw:");
		sb.append(count[WEAK_POSITIVE_INDEX]);
		sb.append(" ns:");
		sb.append(count[STRONG_NEGATIVE_INDEX]);
		sb.append(" nw:");
		sb.append(count[WEAK_NEGATIVE_INDEX]);
		sb.append(" us:");
		sb.append(count[STRONG_NEUTRAL_INDEX]);
		sb.append(" uw:");
		sb.append(count[WEAK_NEUTRAL_INDEX]);
		// sb.append(" bs:");
		// sb.append(count[STRONG_POSNEG_INDEX]);
		// sb.append(" bw:");
		// sb.append(count[WEAK_POSNEG_INDEX]);
		// sb.append(" ] ");
		return sb.toString();
	}

	private List<Feature> detectSentiment(List<Feature> features) {
		List<Feature> result = new ArrayList<Feature>();
		String key;
		Set<MpqaClue> clues;
		String polarity;
		String featurePos;
		String cluePos;
		boolean match = false;

		for (Feature f : features) {

			key = f.term();
			featurePos = MpqaClue.mpqaPos(f.pos());

			clues = mpqa.get(key);
			if (clues != null) {
				for (MpqaClue clue : clues) {

					cluePos = clue.pos();

					match = cluePos.equals(MpqaClue.ANY_PART_OF_SPEECH)
							|| featurePos.equals(cluePos);

					if (!match) {
						continue;
					}

					polarity = clue.polarity();

					if (polarity.equals(MpqaClue.POSITIVE)) {
						f.positiveSentiment(true);
					} else if (polarity.equals(MpqaClue.NEGATIVE)) {
						f.negativeSentiment(true);
					} else if (polarity.equals(MpqaClue.POSITIVE_AND_NEGATIVE)) {
						f.positiveAndNegativeSentiment(true);
					} else if (polarity.equals(MpqaClue.NEUTRAL)) {
						f.neutralSentiment(true);
					}

					// Mark the feature as strongly subjective
					// if at least one matching clue agrees

					if (clue.stronglySubjective()) {
						f.stronglySubjective(true);
					}

				}
			}
			result.add(f);
		}

		return result;
	}

	public List<String> pruned() {
		return Lists.newArrayList(pruned);
	}

	/**
	 * Detect negation such as 'cannot' (MD), 'against' (IN) and 'neither' (DT).
	 * 
	 * @param features
	 * @return
	 */
	private List<Feature> detectNegation(List<Feature> features) {
		List<Feature> result = new ArrayList<Feature>();
		boolean negation = false;
		String term;
		String pos;

		for (Feature f : features) {
			term = f.term();
			pos = f.pos();
			if (negation) {
				if (PUNCTUATION_POS.contains(pos)
						|| NEGATION_TERMINATORS.contains(term)) {
					negation = false;
				} else {
					f.negated(true);
				}
			} else {
				if (negationWords.contains(term)) {
					negation = true;
				}
			}
			result.add(f);
		}

		return result;
	}

	/**
	 * Remove POS irrelevant to sentiment analysis
	 * 
	 * @param text
	 * @return
	 */
	private List<Feature> prunePos(List<Feature> features,
			Set<String> posToPrune) {
		List<Feature> result = new ArrayList<Feature>(features.size());

		for (Feature f : features) {

			if (posToPrune.contains(f.pos())) {

				pruned.add(f.toString());

			} else {
				result.add(f);
			}
		}

		return result;
	}

	private List<Feature> tagPos(String text) {
		String term;
		String pos;
		int delimIndex = 0;
		String tagged = tagger.tagString(text);

		List<Feature> features = new ArrayList<Feature>();
		List<String> tokens = Splitter.on(CharMatcher.WHITESPACE).trimResults()
				.omitEmptyStrings().splitToList(tagged);
		for (String token : tokens) {
			delimIndex = token.lastIndexOf('_');
			checkState(delimIndex + 1 < token.length(),
					"Missing POS tag for token [%s]", token);
			term = token.substring(0, delimIndex);
			pos = token.substring(delimIndex + 1);
			features.add(new Feature(term, pos));
		}

		return features;
	}

	private static String preprocess(String text) {
		// Normalize to lowercase

		String val = Strings.nullToEmpty(text).toLowerCase();

		// Strip noisy characters

		val = CharMatcher.anyOf(NOISE_CHARACTERS).removeFrom(val);

		List<String> tokens = Splitter.on(CharMatcher.WHITESPACE).trimResults()
				.omitEmptyStrings().splitToList(val);

		List<String> result = new ArrayList<String>();

		StringBuffer replacement;

		for (String token : tokens) {
			if (token.startsWith("http")) {
				// do nothing
			} else {

				// Pad punctuation marks with whitespace, for tokenizing later
				// However this also makes emoticon detection impossible
				// TODO preserve emoticons

				for (int i = 0; i < PUNCTUATION_TO_DETECT.length; i++) {
					replacement = new StringBuffer(" ");
					replacement.append(PUNCTUATION_TO_DETECT[i]);
					replacement.append(" ");
					token = CharMatcher.is(PUNCTUATION_TO_DETECT[i])
							.replaceFrom(token, replacement);

				}

				// Strip digits if the token is not a user-mention or hashtag

				if (token.startsWith("#") || token.startsWith("@")) {
					// do nothing
				} else {
					token = CharMatcher.DIGIT.removeFrom(token);
				}
			}
			result.add(token);
		}

		return CharMatcher.WHITESPACE.trimFrom(Joiner.on(' ').join(result));
	}

	private void negationWords(String filePath) throws IOException {
		negationWords = new HashSet<String>();
		File file = new File(filePath);
		BufferedReader br = null;
		String line;

		try {
			System.out.printf("Loading negation words...\n\t%s\n", filePath);
			br = new BufferedReader(new FileReader(file));
			while ((line = br.readLine()) != null) {
				line = CharMatcher.WHITESPACE.trimFrom(line).toLowerCase();
				if (line.startsWith("#")) {
					continue;
				}
				negationWords.add(line);
			}
			System.out.printf("Loaded %s negation words\n",
					negationWords.size());
		} finally {
			if (br != null) {
				br.close();
			}
		}
	}
}
