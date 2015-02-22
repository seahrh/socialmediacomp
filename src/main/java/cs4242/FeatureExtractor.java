package cs4242;

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

	// TODO Retain cardinal numbers such as 'million'
	// TODO Negation in MD such as 'cannot'
	// TODO Negation in IN such as 'against'
	// TODO Negation in DT such as 'neither'
	// TODO Modifiers in . such as '!!'

	private static final Set<String> POS_EXCLUSION = Sets.newHashSet("_``",
			"_''", "_(", "_)", "_,", "_--", "_.", "_:", "_RT", "_CC", "_CD",
			"_DT", "_EX", "_IN", "_LS", "_PDT", "_POS", "_PRP", "_PRP$",
			"_SYM", "_TO", "_WDT", "_WP", "_WP$", "WRB");

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
		String features = "";
		String intermediate = preprocess(text);
		intermediate = tagPos(intermediate);
		intermediate = prunePos(intermediate);

		features = intermediate;
		return features;
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
	private String prunePos(String text) {

		// String term;
		String pos;
		int delimIndex = 0;

		List<String> features = Splitter.on(CharMatcher.WHITESPACE)
				.trimResults().omitEmptyStrings().splitToList(text);

		List<String> result = new ArrayList<String>();

		for (String feature : features) {

			// Split into term and POS tag

			delimIndex = feature.lastIndexOf('_');
			// term = feature.substring(0, delimIndex);

			pos = feature.substring(delimIndex);

			if (POS_EXCLUSION.contains(pos)) {

				pruned.add(feature);
			} else {
				result.add(feature);
			}
		}

		return Joiner.on(' ').join(result);
	}

	private String tagPos(String text) {
		return tagger.tagString(text);
	}

	private static String preprocess(String text) {
		// Normalize to lowercase

		String val = Strings.nullToEmpty(text).toLowerCase();

		// Trim whitespace and single/double quotes enclosing the tweet
		// so that RT can be correctly tagged as retweet instead of NNP

		val = CharMatcher.WHITESPACE.trimFrom(val);

		// Strip noise characters

		val = CharMatcher.anyOf(",;\"()\\&=^~`|{}[]").removeFrom(val);

		return val;
	}

}
