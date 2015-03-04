package cs4242.a1;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.core.tokenizers.WordTokenizer;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;


public class AOneServlet extends HttpServlet {

	private static final Logger log = LoggerFactory
			.getLogger(AOneServlet.class);

	public static final ArrayList<Attribute> ATTRIBUTES = new ArrayList<Attribute>();

	static {
		// Class label attribute is nominal

		// There is a known problem saving SparseInstance objects from datasets
		// that have string attributes. In Weka, string and nominal data values
		// are stored as numbers; these numbers act as indexes into an array of
		// possible attribute values (this is very efficient). However, the
		// first string value is assigned index 0: this means that, internally,
		// this value is stored as a 0. When a SparseInstance is written, string
		// instances with internal value 0 are not output, so their string value
		// is lost (and when the arff file is read again, the default value 0 is
		// the index of a different string value, so the attribute value appears
		// to change). To get around this problem, add a dummy string value at
		// index 0 that is never used whenever you declare string attributes
		// that are likely to be used in SparseInstance objects and saved as
		// Sparse ARFF files.

		List<String> values = ImmutableList.<String> builder()
				.add("dummy", "1", "0", "-1").build();
		ATTRIBUTES.add(new Attribute("class_label", values)); // Index 0

		// Aspect attribute is nominal

		values = ImmutableList
				.<String> builder()
				.add("dummy", "other", "teaparty", "dems", "hcr", "gop",
						"conservatives", "stupak", "liberals", "obama").build();
		ATTRIBUTES.add(new Attribute("aspect", values)); // Index 1

		ATTRIBUTES.add(new Attribute("positive_strong")); // Index 2
		ATTRIBUTES.add(new Attribute("positive_weak")); // Index 3
		ATTRIBUTES.add(new Attribute("negative_strong")); // Index 4
		ATTRIBUTES.add(new Attribute("negative_weak")); // Index 5
		ATTRIBUTES.add(new Attribute("neutral_strong")); // Index 6
		ATTRIBUTES.add(new Attribute("neutral_weak")); // Index 7

	}

	public void doGet(HttpServletRequest req, HttpServletResponse rsp)
			throws IOException, ServletException {
		boolean redirect = false;
		boolean ajax = false;

		ServletContext context = getServletContext();

		String testString = Strings.nullToEmpty(req.getParameter("t"));

		if (!testString.isEmpty()) {
			log.info("test string: {}", testString);
			List<PredictionResult> result = new ArrayList<PredictionResult>();
			FeatureExtractor fe = MyContextListener.featureExtractor(context);
			Instances header = MyContextListener.instancesHeader(context);
			Instances testData = extract(testString, fe, header);
			Map<String, Classifier> sentimentClassifiers = MyContextListener
					.sentimentClassifiers(context);
			Map<String, Classifier> aspectClassifiers = MyContextListener
					.aspectClassifiers(context);

			try {

				result = classify(testData, sentimentClassifiers, "sentiment");
				result.addAll(classify(testData, aspectClassifiers, "aspect"));
			} catch (Exception e) {
				e.printStackTrace();
			}

			String json = new Gson().toJson(result);
			rsp.setCharacterEncoding("UTF-8");
			rsp.setContentType("application/json");
			rsp.getWriter().write(json);
			ajax = true;
		}

		if (!ajax) {
			if (redirect) {
				rsp.sendRedirect("/a1");
			} else {
				req.getRequestDispatcher("/WEB-INF/pages/a1.html").forward(req,
						rsp);
			}
		}
	}

	private static Instances stringToWordVector(Instances in) throws Exception {
		StringToWordVector filter = new StringToWordVector();
		// filter.setOptions(options);
		// filter.setLowerCaseTokens(true);
		filter.setInputFormat(in);

		filter.setTokenizer(tokenizer());

		// TODO should we still use stopwords?
		// filter.setStopwordsHandler(stopwords(stopwordsFilePath));
		filter.setAttributeIndices("last");

		Instances out = Filter.useFilter(in, filter);

		return out;
	}

	private static WordTokenizer tokenizer() {
		WordTokenizer tokenizer = new WordTokenizer();
		tokenizer.setDelimiters(" \r\n\t");
		return tokenizer;
	}

	private Instances extract(String text, FeatureExtractor fe, Instances header) {

		List<Feature> features = fe.extract(text);
		int[] sentimentCount = FeatureExtractor.countSentiment(features);

		ArrayList<Attribute> attributes = new ArrayList<Attribute>(ATTRIBUTES);

		for (Feature f : features) {
			attributes.add(new Attribute(f.toString()));
		}

		Instances data = new Instances("relation-name", attributes, 1);
		data.setClass(attributes.get(0));
		DenseInstance inst = new DenseInstance(attributes.size());
		inst.setValue(attributes.get(2),
				sentimentCount[FeatureExtractor.STRONG_POSITIVE_INDEX]);
		inst.setValue(attributes.get(3),
				sentimentCount[FeatureExtractor.WEAK_POSITIVE_INDEX]);
		inst.setValue(attributes.get(4),
				sentimentCount[FeatureExtractor.STRONG_NEGATIVE_INDEX]);
		inst.setValue(attributes.get(5),
				sentimentCount[FeatureExtractor.WEAK_NEGATIVE_INDEX]);
		inst.setValue(attributes.get(6),
				sentimentCount[FeatureExtractor.STRONG_NEUTRAL_INDEX]);
		inst.setValue(attributes.get(7),
				sentimentCount[FeatureExtractor.WEAK_NEUTRAL_INDEX]);

		for (int i = 8; i < attributes.size(); i++) {
			inst.setValue(attributes.get(i), 1);
		}

		inst.setDataset(data);
		data.add(inst);

		try {
			// data = stringToWordVector(data);
			data = setHeader(data, header);
		} catch (Exception e) {

			log.error(
					"Error in generating Weka instances from test string.\nInstances: {}",
					data);
			throw new IllegalStateException(
					"Error in generating Weka instances from test string.", e);
		}
		return data;

	}

	private static Instances setHeader(Instances in, Instances header)
			throws IOException {
		checkNotNull(header, "Instances header cannot be null");

		Instances out = new Instances(header, in.numInstances());

		List<Attribute> commonAttrs = new ArrayList<Attribute>();
		List<String> commonAttrsDebug = new ArrayList<String>();

		Attribute attr;
		SparseInstance inst;

		for (int i = 0; i < in.numAttributes(); i++) {
			attr = in.attribute(i);
			if (header.attribute(attr.name()) != null) {
				// Attribute exists in both header and input data

				commonAttrs.add(attr);
				commonAttrsDebug.add(attr.name());
			}
		}

		for (Instance input : in) {

			inst = new SparseInstance(1d, new double[header.numAttributes()]);
			inst.setDataset(header);
			for (Attribute ca : commonAttrs) {
				if (input.isMissing(ca)) {
					inst.setMissing(ca);
				} else {
					inst.setValue(ca, input.value(ca));
				}

			}

			out.add(inst);
		}

		return out;
	}

	private List<PredictionResult> classify(Instances testData,
			Map<String, Classifier> classifiers, String target)
			throws Exception {
		String name;
		Classifier cls;
		List<PredictionResult> result = new ArrayList<PredictionResult>(
				classifiers.size());
		double label;
		target = Strings.nullToEmpty(target);

		if (target.equals("sentiment")) {
			testData.setClassIndex(0);
		} else if (target.equals("aspect")) {
			testData.setClassIndex(1);
		} else {
			log.error("Class attribute of the type [{}] is unknown", target);
			// Force exception by setting class attribute to be undefined
			testData.setClassIndex(-1);
		}

		for (Map.Entry<String, Classifier> entry : classifiers.entrySet()) {
			name = entry.getKey();

			cls = entry.getValue();
			label = cls.classifyInstance(testData.firstInstance());
			result.add(new PredictionResult(name, target, label));
		}
		return result;
	}

}
