package cs4242;

import static com.google.common.base.Preconditions.checkState;

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
	// TODO Negation in MD such as 'cannot'
	// TODO Negation in IN such as 'against'
	// TODO Negation in DT such as 'neither'
	// TODO Modifiers in . such as '!!'

	private static final Set<String> POS_EXCLUSION_LIST = Sets.newHashSet("RT",
			"CC", "DT", "EX", "IN", "LS", "PDT", "POS", "PRP", "PRP$", "SYM",
			"TO", "WDT", "WP", "WP$", "WRB");

	private static final Set<String> PUNCTUATION_POS = Sets.newHashSet("``",
			"''", "(", ")", ",", "--", ".", ":");

	// Removed hyphen (-) because don't want to break up words like 'anti-hero'
	// Removed apostrophe (-) because don't want to break up words like 'don't'

	private static final char[] PUNCTUATION_MARKS = "[](){}:,`.!?\";\\/"
			.toCharArray();

	private static final String NOISE_CHARACTERS = "*%&=^~|";

	private Map<String, Set<MpqaClue>> mpqa;
	private MaxentTagger tagger;
	private Set<String> pruned;

	private FeatureExtractor() {
		pruned = new HashSet<String>();
		mpqa = null;
		tagger = null;
	}

	public FeatureExtractor(String taggerPath, String lexiconPath)
			throws IOException {
		this();
		System.out.println("Loading sentiment lexicon...");
		mpqa = MpqaClue.cluesFromFile(lexiconPath);
		System.out.println("Sentiment lexicon loaded successfully.");

		System.out.println("Loading tagger...");
		tagger = new MaxentTagger(taggerPath);
		System.out.println("Tagger loaded successfully.");
	}

	public String extract(String text) {
		return pipeline(text);
	}

	private String pipeline(String text) {

		String intermediate = preprocess(text);
		List<Feature> features = tagPos(intermediate);
		features = prunePos(features, POS_EXCLUSION_LIST);
		features = prunePos(features, PUNCTUATION_POS);

		return Feature.toString(features);
	}

	public List<String> pruned() {
		return Lists.newArrayList(pruned);
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
			if (token.startsWith("http") || token.startsWith("#")
					|| token.startsWith("@")) {
				// do nothing
			} else {

				// TODO remove, this doesn't work
				// Pad punctuation marks with whitespace, for tokenizing later

				for (int i = 0; i < PUNCTUATION_MARKS.length; i++) {
					replacement = new StringBuffer(" ");
					replacement.append(PUNCTUATION_MARKS[i]);
					replacement.append(" ");
					token = CharMatcher.is(PUNCTUATION_MARKS[i]).replaceFrom(
							token, replacement);

				}

				// Strip digits

				token = CharMatcher.DIGIT.removeFrom(token);
			}
			result.add(token);
		}

		return CharMatcher.WHITESPACE.trimFrom(Joiner.on(' ').join(result));
	}
}
