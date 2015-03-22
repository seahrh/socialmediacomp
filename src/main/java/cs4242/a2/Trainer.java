package cs4242.a2;

import java.io.File;
import java.io.IOException;

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
		if (args.length != 3) {
			System.out
					.println("Usage: Trainer <input.arff> <age classifier.model> <gender classifier.model>");
			System.exit(1);
		}

		String trainFilePath = args[0];
		String ageModelPath = args[1];
		String genderModelPath = args[2];
		Instances train;
		FilteredClassifier ageClassifier;
		FilteredClassifier genderClassifier;
		long startTime = System.currentTimeMillis();
		
		try {
			train = instances(trainFilePath);
			
			train.setClass(train.attribute("age"));
			ageClassifier = randomForest(train);
			
			train.setClass(train.attribute("gender"));
			genderClassifier = randomForest(train);
			
			SerializationHelper.write(ageModelPath, ageClassifier);
			
			SerializationHelper.write(genderModelPath, genderClassifier);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		long elapsedTime = System.currentTimeMillis() - startTime;
		System.out.printf("Done! Run time: %ss\n", elapsedTime / 1000);
	}
	
	private static FilteredClassifier randomForest(Instances train) throws Exception {
		FilteredClassifier fc = new FilteredClassifier();
		RandomForest rf = new RandomForest();
		MultiFilter filters = filters(train);
		fc.setFilter(filters);
		fc.setClassifier(rf);
		fc.buildClassifier(train);
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
		int[] attributesToRemove = {0, otherClassAttribute};
		rm.setAttributeIndicesArray(attributesToRemove);
		filters[0] = rm;
		
		// Normalize all numeric values to 0-1 range
		
		Normalize norm = new Normalize();
		filters[1] = norm;
		
		multi.setFilters(filters);
		return multi;
	}

}
