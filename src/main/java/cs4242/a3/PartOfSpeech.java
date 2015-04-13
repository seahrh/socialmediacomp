package cs4242.a3;

import static cs4242.a3.PartOfSpeech.ADJECTIVE_POS;
import static cs4242.a3.PartOfSpeech.ADVERB_POS;
import static cs4242.a3.PartOfSpeech.NOUN_POS;
import static cs4242.a3.PartOfSpeech.OTHERS_POS;
import static cs4242.a3.PartOfSpeech.PRONOUN_POS;
import static cs4242.a3.PartOfSpeech.VERB_POS;
import static cs4242.a3.PartOfSpeech.WH_POS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.common.primitives.Ints;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public final class PartOfSpeech {

	public static final Set<String> SOCIAL_POS = Sets.newHashSet("RT", "URL",
			"HT", "USR");

	public static final Set<String> PUNCTUATION_SYMBOL_POS = Sets.newHashSet(
			"$", "``", "''", "(", ")", ",", "--", ".", ":", "POS", "SYM");

	public static final Set<String> OTHERS_POS = Sets.newHashSet("CC", "CD",
			"DT", "EX", "FW", "IN", "LS", "MD", "PDT", "RP", "TO", "UH");

	public static final Set<String> ADJECTIVE_POS = Sets.newHashSet("JJ",
			"JJR", "JJS");

	public static final Set<String> NOUN_POS = Sets.newHashSet("NN", "NNP",
			"NNPS", "NNS");

	public static final Set<String> PRONOUN_POS = Sets
			.newHashSet("PRP", "PRP$");

	public static final Set<String> ADVERB_POS = Sets.newHashSet("RB", "RBR",
			"RBS");

	public static final Set<String> VERB_POS = Sets.newHashSet("VB", "VBD",
			"VBG", "VBN", "VBP", "VBZ");

	public static final Set<String> WH_POS = Sets.newHashSet("WDT", "WP",
			"WP$", "WRB");

	public static final Set<String> VOCABULARY_WHITELIST;

	private static final MaxentTagger tagger = tagger();

	static {
		Set<String> set = Sets.newHashSet("HT", "CD", "FW",
				"RP", "UH");
		set.addAll(ADJECTIVE_POS);
		set.addAll(NOUN_POS);
		set.addAll(ADVERB_POS);
		set.addAll(VERB_POS);
		VOCABULARY_WHITELIST = set;
	}

	private PartOfSpeech() {
		// Private constructor, not meant to be instantiated
	}

	private static MaxentTagger tagger() {

		MaxentTagger tagger = null;
		String taggerFilePath = System.getProperty("a3.pos.tagger.file.path");
		int threads = Ints
				.tryParse(System.getProperty("a3.pos.tagger.threads"));
		Properties config = new Properties();
		config.setProperty("nthreads", String.valueOf(threads));

		try {
			if (taggerFilePath.endsWith(".zip")) {
				String modelName = Files
						.getNameWithoutExtension(taggerFilePath);
				tagger = new MyMaxentTagger(taggerFilePath, modelName, config);
			} else {

				tagger = new MaxentTagger(taggerFilePath, config);
			}
		} catch (IOException e) {
			// Something went wrong while loading the tagger file
			throw new IllegalArgumentException(e.getMessage(), e);
		}
		return tagger;
	}

	public static String tag(String s) {
		return tagger.tagString(s);
	}

	public static List<Word> tagAsListOfWords(String s) {
		List<Word> words = new ArrayList<Word>();
		Word word = null;
		String tagged = tag(s);
		List<String> tokens = Splitter.on(CharMatcher.WHITESPACE).trimResults()
				.splitToList(tagged);

		for (String token : tokens) {
			if (!token.isEmpty()) {
				word = new Word(token);
				words.add(word);
			}
		}

		return words;
	}

}
