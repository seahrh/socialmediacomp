package cs4242.a3;

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
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.core.tokenizers.WordTokenizer;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;

public class TransitSentimentServlet extends HttpServlet {

	private static final Logger log = LoggerFactory
			.getLogger(TransitSentimentServlet.class);

	public void doGet(HttpServletRequest req, HttpServletResponse rsp)
			throws IOException, ServletException {
		ServletContext context = getServletContext();
		boolean ajax = false;

		

		String testString = Strings.nullToEmpty(req.getParameter("t"));

		if (!testString.isEmpty()) {
			log.info("test string: {}", testString);
			List<PredictionResult> result = new ArrayList<PredictionResult>();
			// FeatureExtractor fe =
			// MyContextListener.featureExtractor(context);
			// Instances header = MyContextListener.instancesHeader(context);
			// Instances testData = extract(testString, fe, header);
			// Map<String, Classifier> sentimentClassifiers = MyContextListener
			// .sentimentClassifiers(context);
			// Map<String, Classifier> aspectClassifiers = MyContextListener
			// .aspectClassifiers(context);

			try {

				// result = classify(testData, sentimentClassifiers,
				// "sentiment");
				// result.addAll(classify(testData, aspectClassifiers,
				// "aspect"));
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

			req.getRequestDispatcher("/WEB-INF/pages/index.html")
					.forward(req, rsp);

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
			result.add(new PredictionResult(name, label));
		}
		return result;
	}

}
