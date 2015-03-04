package cs4242.a1;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
		String taggerPath = context.getRealPath("/WEB-INF/gate-EN-twitter.zip");
		String taggerName = "gate-EN-twitter.model";
		String lexiconPath = context
				.getRealPath("/WEB-INF/subjclueslen1-HLTEMNLP05.tff");
		String negationPath = context.getRealPath("/WEB-INF/negation.txt");
		String headerPath = "/WEB-INF/train.arff";
		String modelsBasePath = "/WEB-INF/models/";
		String s1Filename = "smo_featureselection_gridsearch_train+dev_wekadev.model";
		String s1FilePath = context.getRealPath(modelsBasePath + "smo_featureselection_gridsearch_train+dev_wekadev.zip");
		String s2Filename = "RFsentiment.model";
		String s2FilePath = context.getRealPath(modelsBasePath + "RFsentiment.zip");
		String a1Filename = "aspect_smo_featureselection_gridsearch_train+dev_wekadev.model";
		String a1FilePath = context.getRealPath(modelsBasePath + "aspect_smo_featureselection_gridsearch_train+dev_wekadev.zip");
		String a2Filename = "RFaspect.model";
		String a2FilePath = context.getRealPath(modelsBasePath + "RFaspect.zip");

		FeatureExtractor fe;
		Classifier cls;
		Map<String, Classifier> sentimentClassifiers = new HashMap<String, Classifier>();
		Map<String, Classifier> aspectClassifiers = new HashMap<String, Classifier>();
		Instances header;
		Optional<Classifier> unzippedClassifier;

		try {

			fe = new FeatureExtractor(taggerPath, taggerName, lexiconPath,
					negationPath);

			ArffLoader loader = new ArffLoader();
			loader.setSource(context.getResourceAsStream(headerPath));
			header = loader.getStructure();

			// Load sentiment classifier 1
			
			unzippedClassifier = unzipClassifier(s1FilePath, s1Filename);
			if (unzippedClassifier.isPresent()) {
				sentimentClassifiers.put(s1Filename, unzippedClassifier.get());
			} else {
				log.warn("Classifier [{}] cannot be found in zip file:\n{}", s1Filename, s1FilePath);
			}
			
			// Load sentiment classifier 2
			
			unzippedClassifier = unzipClassifier(s2FilePath, s2Filename);
			if (unzippedClassifier.isPresent()) {
				sentimentClassifiers.put(s2Filename, unzippedClassifier.get());
			} else {
				log.warn("Classifier [{}] cannot be found in zip file:\n{}", s2Filename, s2FilePath);
			}
			
			// Load aspect classifier 1
			
			unzippedClassifier = unzipClassifier(a1FilePath, a1Filename);
			if (unzippedClassifier.isPresent()) {
				aspectClassifiers.put(a1Filename, unzippedClassifier.get());
			} else {
				log.warn("Classifier [{}] cannot be found in zip file:\n{}", a1Filename, a1FilePath);
			}
			
			// Load aspect classifier 2
			
			unzippedClassifier = unzipClassifier(a2FilePath, a2Filename);
			if (unzippedClassifier.isPresent()) {
				aspectClassifiers.put(a2Filename, unzippedClassifier.get());
			} else {
				log.warn("Classifier [{}] cannot be found in zip file:\n{}", a2Filename, a2FilePath);
			}

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

	public static Optional<Classifier> unzipClassifier(String zipFilePath,
			String target) throws Exception {
		File zipFile = new File(zipFilePath);
		InputStream is;
		ZipInputStream zis = null;
		ZipEntry entry;

		try {

			is = new FileInputStream(zipFile);
			zis = new ZipInputStream(is);

			while ((entry = zis.getNextEntry()) != null) {

				// Find zip entry with this name

				if (entry.getName().equals(target)) {

					return Optional.of((Classifier) SerializationHelper
							.read(zis));

				}
			}
		} finally {
			if (zis != null) {
				zis.close();
			}
		}
		return Optional.absent();
	}

}
