package cs4242.a3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.Ranker;
import weka.attributeSelection.CorrelationAttributeEval;
import weka.core.Instances;

public final class FeatureSelection {

	private static final int TOP_K = Integer.getInteger("a3.words.max");

	private static final Set<String> STOP_WORDS = FileUtil.load(System
			.getProperty("a3.stopwords.file.path"));

	private FeatureSelection() {
		// Private constructor, not meant to be instantiated
	}

	public static void main(String[] args) {

		Instances data = null;
		String inFilePath = System.getProperty("a3.input.file.path");
		String outFilePath = System.getProperty("a3.output.file.path");
		String vocabFilePath = System.getProperty("a3.vocab.file.path");
		final String CLASS_NAME = "class";

		long startTime = System.currentTimeMillis();
		try {
			data = ArffGenerator.loadArff(inFilePath);
			data.setClass(data.attribute(CLASS_NAME));
			AttributeSelection selection = new AttributeSelection();
			Ranker ranker = new Ranker();
			ranker.setNumToSelect(TOP_K);
			CorrelationAttributeEval corrEval = new CorrelationAttributeEval();

			selection.setEvaluator(corrEval);
			selection.setSearch(ranker);
			selection.SelectAttributes(data);
			int[] selected = selection.selectedAttributes();
			List<String> vocab = vocab(data, selected);
			FileUtil.save(selection.toResultsString(), outFilePath);
			FileUtil.save(vocab, vocabFilePath);

		} catch (Exception e) {

			e.printStackTrace();
		}

		long elapsedTime = System.currentTimeMillis() - startTime;
		System.out.printf("Done! Run time: %ss\n", elapsedTime / 1000);
	}

	public static List<String> vocab(Instances data, int[] selectedAttributes) {
		Set<String> vocab = new HashSet<String>();
		List<String> vocabList = new ArrayList<String>();
		String wordWithPos;
		String word;
		int separatorIndex = -1;

		for (int i : selectedAttributes) {
			wordWithPos = data.attribute(i).name();
			separatorIndex = wordWithPos.lastIndexOf('_');
			if (separatorIndex > 0) {
				word = wordWithPos.substring(0, separatorIndex);
				if (!STOP_WORDS.contains(word)) {
					vocab.add(wordWithPos);
				}
			}

		}

		vocabList.addAll(vocab);
		return vocabList;
	}

}
