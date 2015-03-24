package cs4242.a2;

import java.io.File;
import java.io.IOException;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;

public final class Tester {

	private Tester() {
		// Private constructor, not meant to be instantiated
	}

	public static void main(String[] args) {
		if (args.length != 4) {
			System.out.println("Usage: ");
			System.exit(1);
		}
		String testPath = args[0];
		String agePath = args[1];
		String genderPath = args[2];
		String workingDir = args[3];
		long startTime = System.currentTimeMillis();
		Instances data = null;
		FilteredClassifier ageClassifier = null;
		FilteredClassifier genderClassifier = null;
		String result = "";

		try {
			data = Trainer.instances(testPath);

			ageClassifier = (FilteredClassifier) SerializationHelper
					.read(agePath);

			genderClassifier = (FilteredClassifier) SerializationHelper
					.read(genderPath);

			result = test(ageClassifier, data, "age");
			result = print(result, "age result");
			save(workingDir, result, "test_age");

			result = test(genderClassifier, data, "gender");
			result = print(result, "gender result");
			save(workingDir, result, "test_gender");

		} catch (Exception e) {
			e.printStackTrace();
		}
		long elapsedTime = System.currentTimeMillis() - startTime;
		System.out.printf("Done! Run time: %ss\n", elapsedTime / 1000);
	}

	private static String test(Classifier cls, Instances data, String classAttributeName) throws Exception {

		Attribute classAttribute = data.attribute(classAttributeName);
		data.setClass(classAttribute);
		Evaluation eval = new Evaluation(data);
		double[] predictions = eval.evaluateModel(cls, data);
		StringBuilder sb = new StringBuilder();
		sb.append(eval.toSummaryString());
		sb.append("\n");
		sb.append(eval.toClassDetailsString());
		sb.append("\n");
		sb.append(eval.toMatrixString());
		sb.append("\nPredictions:\n\n");
		
		Instance inst = null;
		String userId = "";
		String prediction = "";
		
		for (int i = 0; i < predictions.length; i++) {
			inst = data.get(i);
			userId = inst.stringValue(0);
			prediction = classAttribute.value((int) predictions[i]);
			sb.append(userId);
			sb.append("\t");
			sb.append(prediction);
			sb.append("\n");
		}
		
		String result = sb.toString();

		return result;
	}

	private static String print(String result, String title) {
		StringBuilder sb = new StringBuilder();
		sb.append("\n=============================\n");
		sb.append(title.toUpperCase());
		sb.append("\n=============================\n");
		sb.append(result);
		String out = sb.toString();
		System.out.println(out);
		return out;
	}

	private static void save(String outDir, String result, String fileName)
			throws IOException {
		StringBuilder sb = new StringBuilder(outDir).append(File.separator);
		sb.append("");
		sb.append(fileName);
		sb.append(".txt");
		String path = sb.toString();
		FileUtil.save(result, path);
	}

}
