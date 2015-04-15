package cs4242.a3;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.classifiers.Classifier;
import weka.core.SerializationHelper;

import com.google.common.base.Optional;
import com.google.common.io.Files;

public class MyContextListener implements ServletContextListener {

	private static final Logger log = LoggerFactory
			.getLogger(MyContextListener.class);

	public static final String RANDOM_FOREST_CLASSIFIER = "randomforest";
	public static final String SVM_CLASSIFIER = "svm";
	public static final String NAIVE_BAYES_CLASSIFIER = "naivebayes";
	public static final String STACK_ENSEMBLE_CLASSIFIER = "stackensemble";

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
		
		// File paths
		
		String lexiconPath = context.getRealPath(System.getProperty("a3.sentiment.mpqa.file.path"));
		String vocabPath = context.getRealPath(System.getProperty("a3.vocab.file.path"));
		String posTaggerPath = context.getRealPath(System.getProperty("a3.pos.tagger.file.path"));
		String randomForestPath = context.getRealPath(System.getProperty("a3.models.randomforest.file.path"));
		String svmPath = context.getRealPath(System.getProperty("a3.models.svm.file.path"));
		String naiveBayesPath = context.getRealPath(System.getProperty("a3.models.naivebayes.file.path"));
		String stackPath = context.getRealPath(System.getProperty("a3.models.stack.file.path"));
		String randomForestModelName = Files.getNameWithoutExtension(randomForestPath) + ".model";
		String svmModelName = Files.getNameWithoutExtension(svmPath) + ".model";
		String naiveBayesModelName = Files.getNameWithoutExtension(naiveBayesPath) + ".model";
		String stackModelName = Files.getNameWithoutExtension(stackPath) + ".model";
		
		// Initialize sentiment lexicon
		
		Sentiment.init(lexiconPath);
		
		// Initialize vocabulary
		
		Vocabulary.init(vocabPath);
		
		// Initialize POS tagger
		
		PartOfSpeech.init(posTaggerPath);
		
		
		
	

		
		Classifier cls;
		
		Optional<Classifier> unzippedClassifier;

		try {

			

			//ArffLoader loader = new ArffLoader();
			//loader.setSource(context.getResourceAsStream(headerPath));
			//header = loader.getStructure();

			// Load random forest classifier
			
			unzippedClassifier = unzipClassifier(randomForestPath, randomForestModelName);
			if (unzippedClassifier.isPresent()) {
				context.setAttribute(RANDOM_FOREST_CLASSIFIER, unzippedClassifier.get());
			} else {
				log.warn("Classifier cannot be found in zip file:\nClassifier:{}\nZip file:{}", randomForestModelName, randomForestPath);
			}
			
			// Load svm classifier
			
			unzippedClassifier = unzipClassifier(svmPath, svmModelName);
			if (unzippedClassifier.isPresent()) {
				context.setAttribute(SVM_CLASSIFIER, unzippedClassifier.get());
			} else {
				log.warn("Classifier cannot be found in zip file:\nClassifier:{}\nZip file:{}", svmModelName, svmPath);
			}
			
			// Load naive bayes classifier
			
			unzippedClassifier = unzipClassifier(naiveBayesPath, naiveBayesModelName);
			if (unzippedClassifier.isPresent()) {
				context.setAttribute(NAIVE_BAYES_CLASSIFIER, unzippedClassifier.get());
			} else {
				log.warn("Classifier cannot be found in zip file:\nClassifier:{}\nZip file:{}", naiveBayesModelName, naiveBayesPath);
			}
			
			// Load stack ensemble classifier
			
			unzippedClassifier = unzipClassifier(stackPath, stackModelName);
			if (unzippedClassifier.isPresent()) {
				context.setAttribute(STACK_ENSEMBLE_CLASSIFIER, unzippedClassifier.get());
			} else {
				log.warn("Classifier cannot be found in zip file:\nClassifier:{}\nZip file:{}", stackModelName, stackPath);
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException(
					"Some error occurred when initializing servlet context.",
					e);
		}
		
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
