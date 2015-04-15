package cs4242.a3;

import static cs4242.a3.StringUtil.trim;
import static cs4242.a3.MyContextListener.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.classifiers.Classifier;
import weka.core.Instances;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.gson.Gson;

public class TransitSentimentServlet extends HttpServlet {

	private static final Logger log = LoggerFactory
			.getLogger(TransitSentimentServlet.class);

	public void doGet(HttpServletRequest req, HttpServletResponse rsp)
			throws IOException, ServletException {
		ServletContext context = getServletContext();
		

		String text = trim(req.getParameter("t"));

		if (!text.isEmpty()) {
			log.info("Text input: [{}]", text);
			
			//int lexicalErrors = SpellChecker.countLexicalErrors(text);
			int lexicalErrors = 1;
			List<FeatureVector> input = new ArrayList<FeatureVector>(1);
			FeatureVector fv = new FeatureVector(text, lexicalErrors);
			input.add(fv);
			Set<String> ids = Sets.newHashSet();
			Instances test = FeatureVector.getInstances(input, ids);
			test.setClass(test.attribute("class"));
			List<PredictionResult> results = new ArrayList<PredictionResult>();
			PredictionResult result;
			double classValue = 0;
			Classifier cls;
			try {
				
				cls = (Classifier) context.getAttribute(STACK_ENSEMBLE_CLASSIFIER);
				classValue = cls.classifyInstance(test.firstInstance());
				result = new PredictionResult(STACK_ENSEMBLE_CLASSIFIER, classValue);
				results.add(result);
				log.info("Stack ensemble predicts: [{}]", classValue);
				
				cls = (Classifier) context.getAttribute(RANDOM_FOREST_CLASSIFIER);
				classValue = cls.classifyInstance(test.firstInstance());
				result = new PredictionResult(RANDOM_FOREST_CLASSIFIER, classValue);
				results.add(result);
				log.info("Random forest predicts: [{}]", classValue);
				
				cls = (Classifier) context.getAttribute(SVM_CLASSIFIER);
				classValue = cls.classifyInstance(test.firstInstance());
				result = new PredictionResult(SVM_CLASSIFIER, classValue);
				results.add(result);
				log.info("SVM predicts: [{}]", classValue);
				
				
				cls = (Classifier) context.getAttribute(NAIVE_BAYES_CLASSIFIER);
				classValue = cls.classifyInstance(test.firstInstance());
				result = new PredictionResult(NAIVE_BAYES_CLASSIFIER, classValue);
				results.add(result);
				log.info("Naive Bayes predicts: [{}]", classValue);
				
				
				
			} catch (Exception e) {
				log.warn("Failed to classify instance", e);
			}

			String json = new Gson().toJson(results);
			rsp.setCharacterEncoding("UTF-8");
			rsp.setContentType("application/json");
			rsp.getWriter().write(json);
			return;
		}

		req.getRequestDispatcher("/WEB-INF/pages/index.html").forward(req, rsp);

	}

	

}
