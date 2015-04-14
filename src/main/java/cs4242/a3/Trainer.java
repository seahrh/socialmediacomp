package cs4242.a3;

import static cs4242.a3.FileUtil.save;

import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayesMultinomialUpdateable;
import weka.classifiers.functions.SMO;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.meta.Stacking;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.filters.Filter;
import weka.filters.MultiFilter;
import weka.filters.unsupervised.attribute.Normalize;
import weka.filters.unsupervised.attribute.Remove;

public final class Trainer {

	private static final int FOLDS = 10;
	private static final int SEED = 1;
	private static final int THREADS = 3;

	private Trainer() {
		// Private constructor, not meant to be instantiated
	}

	public static void main(String[] args) {
		Instances data = null;
		String inFilePath = System.getProperty("a3.input.file.path");

		String rfFilePath = System
				.getProperty("a3.randomforest.eval.file.path");
		String svmFilePath = System.getProperty("a3.svm.eval.file.path");
		String nbFilePath = System.getProperty("a3.naivebayes.eval.file.path");
		String stackingFilePath = System
				.getProperty("a3.stacking.eval.file.path");

		String rfModelPath = System
				.getProperty("a3.randomforest.model.file.path");
		String svmModelPath = System.getProperty("a3.svm.model.file.path");
		String nbModelPath = System
				.getProperty("a3.naivebayes.model.file.path");
		String stackingModelPath = System
				.getProperty("a3.stacking.model.file.path");

		final String CLASS_NAME = "class";
		FilteredClassifier stackedEnsemble;
		FilteredClassifier randomForest;
		FilteredClassifier svm;
		FilteredClassifier naiveBayes;
		long startTime = System.currentTimeMillis();

		try {

			data = ArffGenerator.loadArff(inFilePath);
			data.setClass(data.attribute(CLASS_NAME));

			randomForest = randomForest(data);
			saveValidationResult(randomForest, data, CLASS_NAME, rfFilePath);

			// Reset the classifier
			// Return the model that is trained on the entire train set

			randomForest = randomForest(data);
			randomForest.buildClassifier(data);
			saveModel(randomForest, rfModelPath);

			svm = svm(data);
			saveValidationResult(svm, data, CLASS_NAME, svmFilePath);
			svm = svm(data);
			svm.buildClassifier(data);
			saveModel(svm, svmModelPath);

			naiveBayes = naiveBayes(data);
			saveValidationResult(naiveBayes, data, CLASS_NAME, nbFilePath);
			naiveBayes = naiveBayes(data);
			naiveBayes.buildClassifier(data);
			saveModel(naiveBayes, nbModelPath);

			// Reset all base learners (untrained)
			
			randomForest = randomForest(data);
			svm = svm(data);
			naiveBayes = naiveBayes(data);
			Classifier[] baseLearners = { randomForest, svm, naiveBayes };

			stackedEnsemble = stackingEnsemble(baseLearners, data);
			saveValidationResult(stackedEnsemble, data, CLASS_NAME,
					stackingFilePath);
			stackedEnsemble = stackingEnsemble(baseLearners, data);
			stackedEnsemble.buildClassifier(data);
			saveModel(stackedEnsemble, stackingModelPath);

		} catch (Exception e) {
			e.printStackTrace();
		}

		long elapsedTime = System.currentTimeMillis() - startTime;
		System.out.printf("Done! Run time: %ss\n", elapsedTime / 1000);

	}

	private static void saveModel(Classifier cls, String filePath)
			throws Exception {

		SerializationHelper.write(filePath, cls);
		System.out.printf("Saved model: %s\n", filePath);
	}

	private static MultiFilter filters(Instances data) {
		MultiFilter multi = new MultiFilter();

		// Remove user id and either gender/age attribute
		// depending on which one is the class attribute

		Remove rm = new Remove();

		int[] attributesToRemove = { data.attribute("id").index() };
		rm.setAttributeIndicesArray(attributesToRemove);

		// Normalize all numeric values to 0-1 range

		Normalize norm = new Normalize();

		Filter[] filters = { rm, norm };
		multi.setFilters(filters);
		return multi;
	}

	private static void saveValidationResult(FilteredClassifier fc,
			Instances train, String className, String outFilePath)
			throws Exception {

		train.setClass(train.attribute(className));

		StringBuilder sb = new StringBuilder("Class attribute name: ");
		sb.append(className);
		sb.append("\nValidation results\n");
		String result = validate(fc, train, FOLDS, SEED);
		sb.append(result);
		String output = sb.toString();
		System.out.println(output);

		save(output, outFilePath);

	}

	private static String validate(FilteredClassifier fc, Instances train,
			int folds, int seed) throws Exception {

		Evaluation eval = new Evaluation(train);
		eval.crossValidateModel(fc, train, folds, new Random(seed));
		StringBuilder sb = new StringBuilder();
		sb.append(eval.toSummaryString());
		sb.append("\n");
		sb.append(eval.toClassDetailsString());
		sb.append("\n");
		sb.append(eval.toMatrixString());
		sb.append("\n");
		return sb.toString();
	}

	private static FilteredClassifier randomForest(Instances train)
			throws Exception {
		FilteredClassifier fc = new FilteredClassifier();
		RandomForest rf = new RandomForest();
		rf.setNumTrees(2000);

		MultiFilter filters = filters(train);
		fc.setFilter(filters);
		fc.setClassifier(rf);
		return fc;
	}

	private static FilteredClassifier svm(Instances train) throws Exception {
		FilteredClassifier fc = new FilteredClassifier();
		SMO svm = new SMO();
		svm.setChecksTurnedOff(true);
		svm.setBuildLogisticModels(true);

		MultiFilter filters = filters(train);
		fc.setFilter(filters);
		fc.setClassifier(svm);
		return fc;
	}

	private static FilteredClassifier naiveBayes(Instances train)
			throws Exception {
		FilteredClassifier fc = new FilteredClassifier();
		NaiveBayesMultinomialUpdateable nb = new NaiveBayesMultinomialUpdateable();
		

		MultiFilter filters = filters(train);
		fc.setFilter(filters);
		fc.setClassifier(nb);
		return fc;
	}

	private static FilteredClassifier stackingEnsemble(
			Classifier[] baseLearners, Instances train) throws Exception {
		FilteredClassifier fc = new FilteredClassifier();
		Stacking ensemble = new Stacking();
		ensemble.setNumExecutionSlots(THREADS);
		ensemble.setClassifiers(baseLearners);

		MultiFilter filters = filters(train);
		fc.setFilter(filters);
		fc.setClassifier(ensemble);
		return fc;
	}

}
