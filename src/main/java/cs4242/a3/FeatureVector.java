package cs4242.a3;

import static com.google.common.base.Preconditions.checkNotNull;
import static cs4242.a3.StringUtil.trim;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.languagetool.JLanguageTool;
import org.languagetool.language.English;
import org.languagetool.rules.RuleMatch;

import weka.core.Attribute;
import weka.core.Instances;
import weka.core.SparseInstance;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Doubles;

public class FeatureVector {

	// private static final

	private static ArrayList<Attribute> attributes;
	private static Map<String, Integer> attrIndices;

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
		this.text = trim(text).toLowerCase();
	}

	public FeatureVector(String clazz, String id, String text) {
		this(text);
		checkNotNull(clazz, "class must not be null");
		checkNotNull(id, "id must not be null");
		this.clazz = clazz;
		this.id = id;
	}

	public static Instances getInstances(List<FeatureVector> featureVectors,
			Set<String> ids) throws IOException {
		Instances data = header(ids);
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

		// Lexical errors

		val = SpellChecker.countLexicalErrors(text);
		attrValues.put("lexical_errors", val);

		inst.setValue(header.attribute("class"), clazz);
		inst.setValue(header.attribute("id"), id);

		for (Map.Entry<String, Double> entry : attrValues.entrySet()) {
			attrName = entry.getKey();
			val = entry.getValue();
			attr = header.attribute(attrName);
			inst.setValue(attr, val);
		}

		return inst;
	}

	public static Instances header(Set<String> ids) {

		final String RELATION_NAME = "A3 features";
		final int CAPACITY = 2000;
		Instances header = new Instances(RELATION_NAME, attributes(ids),
				CAPACITY);
		header.setClassIndex(attrIndices.get("class"));
		return header;
	}

	private static ArrayList<Attribute> attributes(Set<String> ids) {

		attributes = new ArrayList<Attribute>();
		attrIndices = new HashMap<String, Integer>();

		// Class attribute

		List<String> values = ImmutableList
				.<String> builder()
				.add("dummy", "rail-positive", "rail-neutral", "rail-negative",
						"taxi-positive", "taxi-neutral", "taxi-negative",
						"bus-positive", "bus-neutral", "bus-negative",
						"not-relevant").build();
		attributes.add(new Attribute("class", values));
		attrIndices.put("class", attributes.size() - 1);

		// Tweet Id attribute

		values = new ArrayList<String>(ids.size() + 1);
		values.add("dummy");
		values.addAll(ids);
		attributes.add(new Attribute("id", values));
		attrIndices.put("id", attributes.size() - 1);

		// Lexical errors attribute

		attributes.add(new Attribute("lexical_errors", values));
		attrIndices.put("lexical_errors", attributes.size() - 1);

		return attributes;
	}
}
