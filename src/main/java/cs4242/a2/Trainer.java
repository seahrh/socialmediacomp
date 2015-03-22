package cs4242.a2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ArffLoader;
import weka.filters.Filter;
import weka.filters.MultiFilter;
import weka.filters.unsupervised.attribute.Normalize;
import weka.filters.unsupervised.attribute.Remove;

public final class Trainer {

	private Trainer() {
		// Private constructor, not meant to be instantiated
	}

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out
					.println("Usage: Trainer <input.arff> <output directory>");
			System.exit(1);
		}

		String trainFilePath = args[0];
		String outDirPath = args[1];
		Instances train;
		FilteredClassifier ageClassifier;
		FilteredClassifier genderClassifier;
		long startTime = System.currentTimeMillis();

		try {
			train = instances(trainFilePath);

			ageClassifier = randomForest(train, "age", outDirPath);

			genderClassifier = randomForest(train, "gender", outDirPath);

			String modelName = "text_age";
			saveModel(outDirPath, modelName, ageClassifier);
			
			modelName = "text_gender";
			saveModel(outDirPath, modelName, genderClassifier);

		} catch (Exception e) {
			e.printStackTrace();
		}

		long elapsedTime = System.currentTimeMillis() - startTime;
		System.out.printf("Done! Run time: %ss\n", elapsedTime / 1000);
	}

	private static FilteredClassifier randomForest(Instances train,
			String classAttributeName, String outDir) throws Exception {
		train.setClass(train.attribute(classAttributeName));

		StringBuilder sb = new StringBuilder("Class attribute name: ");
		sb.append(classAttributeName);
		sb.append("\nValidation results\n");
		String result = validateRandomForest(train);
		sb.append(result);
		String output = sb.toString();
		System.out.println(output);
		
		sb = new StringBuilder(outDir);
		sb.append(File.separator);
		sb.append("text_validation_");
		sb.append(classAttributeName);
		sb.append(".txt");
		String path = sb.toString();
		
		save(output, path);

		// Return the model that is trained on the entire train set
		return trainRandomForest(train);
	}

	private static String validateRandomForest(Instances train)
			throws Exception {
		final int K_FOLDS = 10;
		final int SEED = 1;
		FilteredClassifier fc = randomForestClassifier(train);
		Evaluation eval = new Evaluation(train);
		eval.crossValidateModel(fc, train, K_FOLDS, new Random(SEED));
		StringBuilder sb = new StringBuilder();
		sb.append(eval.toSummaryString());
		sb.append("\n");
		sb.append(eval.toClassDetailsString());
		sb.append("\n");
		sb.append(eval.toMatrixString());
		sb.append("\n");
		return sb.toString();
	}

	private static FilteredClassifier trainRandomForest(Instances train)
			throws Exception {
		FilteredClassifier fc = randomForestClassifier(train);
		fc.buildClassifier(train);
		return fc;
	}

	private static FilteredClassifier randomForestClassifier(Instances train)
			throws Exception {
		FilteredClassifier fc = new FilteredClassifier();
		RandomForest rf = new RandomForest();
		rf.setNumTrees(2000);
		
		MultiFilter filters = filters(train);
		fc.setFilter(filters);
		fc.setClassifier(rf);
		return fc;
	}

	private static Instances instances(String path) throws IOException {
		ArffLoader loader = new ArffLoader();
		File file = new File(path);
		loader.setFile(file);
		return loader.getDataSet();
	}

	private static MultiFilter filters(Instances data) {
		MultiFilter multi = new MultiFilter();
		final int NUMBER_OF_FILTERS = 2;
		Filter[] filters = new Filter[NUMBER_OF_FILTERS];

		// Remove user id and either gender/age attribute
		// depending on which one is the class attribute

		Remove rm = new Remove();
		int otherClassAttribute = -1;
		int classAttribute = data.classIndex();
		if (classAttribute == 1) {
			otherClassAttribute = 2;
		} else if (classAttribute == 2) {
			otherClassAttribute = 1;
		}
		int[] attributesToRemove = { 0, otherClassAttribute };
		rm.setAttributeIndicesArray(attributesToRemove);
		filters[0] = rm;

		// Normalize all numeric values to 0-1 range

		Normalize norm = new Normalize();
		filters[1] = norm;

		multi.setFilters(filters);
		return multi;
	}

	private static void save(String output, String filePath) throws IOException {
		File file = new File(filePath);
		BufferedWriter bw = null;

		try {
			bw = new BufferedWriter(new FileWriter(file));
			bw.write(output);

		} finally {
			if (bw != null) {
				bw.close();
			}
		}
		System.out.printf("Saved file: %s\n", filePath);
	}
	
	private static void saveModel(String outDir, String modelName, Classifier cls) throws Exception {
		StringBuilder sb = new StringBuilder(outDir);
		sb.append(File.separator);
		sb.append(modelName);
		sb.append(".model");
		String path = sb.toString();
		SerializationHelper.write(path, cls);
		System.out.printf("Saved model: %s\n", path);
	}

}
