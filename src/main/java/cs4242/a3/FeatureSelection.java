package cs4242.a3;

import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.Ranker;
import weka.attributeSelection.CorrelationAttributeEval;
import weka.core.Instances;

public final class FeatureSelection {

	private FeatureSelection() {
		// Private constructor, not meant to be instantiated
	}
	
	public static void main(String[] args) {
		
		Instances data = null;
		String inFilePath = System.getProperty("a3.input.file.path");
		String outFilePath = System.getProperty("a3.output.file.path");
		final String CLASS_NAME = "class";
		//final int TOP_K = 100; 
		long startTime = System.currentTimeMillis();
		try {
			data = ArffGenerator.loadArff(inFilePath);
			data.setClass(data.attribute(CLASS_NAME));
			AttributeSelection selection = new  AttributeSelection(); 
		    Ranker ranker = new Ranker();
		    //ranker.setNumToSelect(TOP_K);
		    CorrelationAttributeEval corrEval = new CorrelationAttributeEval();
		    
		    selection.setEvaluator(corrEval);
		    selection.setSearch(ranker);
		    selection.SelectAttributes(data);
		    ArffGenerator.spool(selection.toResultsString(), outFilePath);
		    
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
		

		long elapsedTime = System.currentTimeMillis() - startTime;
		System.out.printf("Done! Run time: %ss\n", elapsedTime / 1000);
	}

}
