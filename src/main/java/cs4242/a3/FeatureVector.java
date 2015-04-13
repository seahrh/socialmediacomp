package cs4242.a3;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static cs4242.a3.PartOfSpeech.*;
import static cs4242.a3.StringUtil.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import weka.core.Attribute;
import weka.core.Instances;
import weka.core.SparseInstance;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

public class FeatureVector {

	

	// private static ArrayList<Attribute> attributes;
	// private static Map<String, Integer> attrIndices;

	private String clazz;
	private String id;
	private String text;
	private Map<String, Double> attrValues;

	public FeatureVector() {
		clazz = "";
		id = "";
		text = "";
		attrValues = new HashMap<String, Double>();
	}

	public FeatureVector(String text) {
		this();
		this.text = CONTROL_CHARACTERS.removeFrom(trim(text));
	}

	public FeatureVector(String clazz, String id, String text) {
		this(text);
		checkNotNull(clazz, "class must not be null");
		checkNotNull(id, "id must not be null");
		this.clazz = clazz;
		this.id = id;
	}

	public FeatureVector(String clazz, String id, String text, int lexicalErrors) {
		this(clazz, id, text);
		checkArgument(lexicalErrors >= 0,
				"Lexical errors must not be a negative number. [%s]",
				lexicalErrors);
		attrValues.put("lexical_errors", (double) lexicalErrors);
	}

	public static Instances getInstances(List<FeatureVector> featureVectors,
			Set<String> ids) throws IOException {
		Set<String> vocab = new HashSet<String>();

		for (FeatureVector fv : featureVectors) {
			// data.add(fv.getInstance(data));
			vocab.addAll(fv.bagOfWords());
		}

		ArrayList<Attribute> attrs = attributes(ids);
		attrs.addAll(bagOfWordsAttributes(vocab));
		Instances data = header(attrs);

		for (FeatureVector fv : featureVectors) {
			data.add(fv.getInstance(data));

		}
		return data;
	}

	public SparseInstance getInstance(Instances header) throws IOException {

		SparseInstance inst = new SparseInstance(1);
		inst.setDataset(header);
		double val = 0;
		Attribute attr = null;
		String attrName = "";

		// Class and tweet id

		inst.setValue(header.attribute("class"), clazz);
		inst.setValue(header.attribute("id"), id);

		// #Lexical errors
		// Invoke spellchecker if the value is not present

		if (!attrValues.containsKey("lexical_errors")) {
			val = SpellChecker.countLexicalErrors(text);
			attrValues.put("lexical_errors", val);
		}

		// #Uppercase chars

		val = countUpper(text);
		attrValues.put("uppercase_characters", val);

		// #Punctuation or symbol chars

		val = countPunctuationSymbol(text);
		attrValues.put("punctuation_symbol_characters", val);

		// Text length

		val = text.length();
		attrValues.put("text_length", val);

		// System.out.printf("class:%s, id:%s\n", clazz, id);

		for (Map.Entry<String, Double> entry : attrValues.entrySet()) {
			attrName = entry.getKey();
			val = entry.getValue();
			attr = header.attribute(attrName);
			inst.setValue(attr, val);
		}

		return inst;
	}

	private Set<String> bagOfWords() {
		List<Word> words = PartOfSpeech.tagAsListOfWords(text);
		Set<String> vocab = new HashSet<String>();
		String v = "";
		for (Word word : words) {
			if (validVocab(word)) {
				v = word.toString();
				attrValues.put(v, 1d);
				vocab.add(v);
			}
		}
		return vocab;
	}

	public static boolean validVocab(Word word) {
		
		String pos = word.pos();
		if (VOCABULARY_WHITELIST.contains(pos) && word.hasLetter()) {
			return true;
		}
		return false;
	}

	public static Instances header(ArrayList<Attribute> attributes) {

		final String RELATION_NAME = "A3 features";
		final int CAPACITY = 2000;
		Instances header = new Instances(RELATION_NAME, attributes, CAPACITY);
		header.setClass(header.attribute("class"));
		return header;
	}

	private static ArrayList<Attribute> attributes(Set<String> ids) {

		final String DUMMY = "dummy";
		String name = "";
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		// attrIndices = new HashMap<String, Integer>();

		// Class (nominal)

		name = "class";
		List<String> values = ImmutableList
				.<String> builder()
				.add(DUMMY, "rail-positive", "rail-neutral", "rail-negative",
						"taxi-positive", "taxi-neutral", "taxi-negative",
						"bus-positive", "bus-neutral", "bus-negative",
						"not-relevant").build();
		attributes.add(new Attribute(name, values));
		// attrIndices.put(name, attributes.size() - 1);

		// Tweet Id (nominal)

		name = "id";
		values = new ArrayList<String>(ids.size() + 1);
		values.add(DUMMY);
		values.addAll(ids);
		attributes.add(new Attribute(name, values));
		// attrIndices.put(name, attributes.size() - 1);

		// Lexical errors (numeric)

		name = "lexical_errors";
		attributes.add(new Attribute(name));
		// attrIndices.put(name, attributes.size() - 1);

		// #Uppercase characters (numeric)

		name = "uppercase_characters";
		attributes.add(new Attribute(name));
		// attrIndices.put(name, attributes.size() - 1);

		// #Punctuation or symbol characters (numeric)

		name = "punctuation_symbol_characters";
		attributes.add(new Attribute(name));
		// attrIndices.put(name, attributes.size() - 1);

		// #Punctuation or symbol characters (numeric)

		name = "text_length";
		attributes.add(new Attribute(name));
		// attrIndices.put(name, attributes.size() - 1);

		return attributes;
	}

	private static ArrayList<Attribute> bagOfWordsAttributes(Set<String> vocab) {
		ArrayList<Attribute> attributes = new ArrayList<Attribute>(vocab.size());
		for (String name : vocab) {
			attributes.add(new Attribute(name));
			// attrIndices.put(name, attributes.size() - 1);
		}
		return attributes;
	}
}
