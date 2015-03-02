package cs4242.web;

import java.io.DataInputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ArffLoader;

import com.google.common.base.Optional;

import cs4242.FeatureExtractor;

public class MyContextListener implements ServletContextListener {

	private static final Logger log = LoggerFactory
			.getLogger(MyContextListener.class);

	private static final String FEATURE_EXTRACTOR = "fe";
	private static final String INSTANCES_HEADER = "header";
	private static final String SENTIMENT_CLASSIFIERS = "sentiment";
	private static final String ASPECT_CLASSIFIERS = "aspect";

	/*
	 * This will be invoked as part of a warmup request, or the first user
	 * request if no warmup request was invoked.
	 * 
	 * @see
	 * javax.servlet.ServletContextListener#contextInitialized(javax.servlet
	 * .ServletContextEvent)
	 */
	@Override
	public void contextInitialized(ServletContextEvent event) {
		ServletContext context = event.getServletContext();
		String taggerPath = context
				.getRealPath("/WEB-INF/gate-EN-twitter.zip");
		String taggerName = "gate-EN-twitter.model";
		String lexiconPath = context
				.getRealPath("/WEB-INF/subjclueslen1-HLTEMNLP05.tff");
		String negationPath = context.getRealPath("/WEB-INF/negation.txt");
		String headerPath = "/WEB-INF/train.arff";
		String modelsBasePath = "/WEB-INF/models/";
		String s1Filename = "smo_featureselection_gridsearch_train+dev_wekadev.model";
		String s2Filename = "sentimentNew.model";
		String a1Filename = "aspect_smo_featureselection_gridsearch_train+dev_wekadev.model";
		String a2Filename = "aspectNew.model";

		FeatureExtractor fe;
		Classifier cls;
		Map<String, Classifier> sentimentClassifiers = new HashMap<String, Classifier>();
		Map<String, Classifier> aspectClassifiers = new HashMap<String, Classifier>();
		Instances header;
		Optional<DataInputStream> taggerInput;

		try {
			
			
			fe = new FeatureExtractor(taggerPath, taggerName, lexiconPath, negationPath);

			ArffLoader loader = new ArffLoader();
			loader.setSource(context.getResourceAsStream(headerPath));
			header = loader.getStructure();

			cls = (Classifier) SerializationHelper.read(context
					.getResourceAsStream(modelsBasePath + s1Filename));
			sentimentClassifiers.put(s1Filename, cls);
			cls = (Classifier) SerializationHelper.read(context
					.getResourceAsStream(modelsBasePath + s2Filename));
			sentimentClassifiers.put(s2Filename, cls);
			cls = (Classifier) SerializationHelper.read(context
					.getResourceAsStream(modelsBasePath + a1Filename));
			aspectClassifiers.put(a1Filename, cls);
			cls = (Classifier) SerializationHelper.read(context
					.getResourceAsStream(modelsBasePath + a2Filename));
			aspectClassifiers.put(a2Filename, cls);

		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException(
					"Some error occurred when initializing servlet context. Loading feature extractor and classifers from file.",
					e);
		}
		context.setAttribute(FEATURE_EXTRACTOR, fe);
		context.setAttribute(INSTANCES_HEADER, header);
		context.setAttribute(SENTIMENT_CLASSIFIERS, sentimentClassifiers);
		context.setAttribute(ASPECT_CLASSIFIERS, aspectClassifiers);
		log.info("Servlet context initialized.");
	}

	/*
	 * App Engine does not currently invoke this method.
	 * 
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.
	 * ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(ServletContextEvent event) {
		// NOOP.
	}

	public static FeatureExtractor featureExtractor(ServletContext context) {
		return (FeatureExtractor) context.getAttribute(FEATURE_EXTRACTOR);
	}

	public static Instances instancesHeader(ServletContext context) {
		return (Instances) context.getAttribute(INSTANCES_HEADER);
	}

	public static Map<String, Classifier> sentimentClassifiers(
			ServletContext context) {
		@SuppressWarnings("unchecked")
		Map<String, Classifier> result = (Map<String, Classifier>) context
				.getAttribute(SENTIMENT_CLASSIFIERS);
		return result;
	}

	public static Map<String, Classifier> aspectClassifiers(
			ServletContext context) {
		@SuppressWarnings("unchecked")
		Map<String, Classifier> result = (Map<String, Classifier>) context
				.getAttribute(ASPECT_CLASSIFIERS);
		return result;
	}

}
