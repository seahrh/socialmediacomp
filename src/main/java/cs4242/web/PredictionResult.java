package cs4242.web;

public class PredictionResult {
	
	private String modelName;
	private String type;
	private String classLabel;
	
	private PredictionResult() {
		modelName = null;
		type = null;
		classLabel = null;
	}
	
	public PredictionResult(String modelName, String type, double classValue) {
		this.modelName = modelName;
		this.type = type;
		this.classLabel = classLabel(type, classValue);
	}
	
	private static String classLabel(String type, double classValue) {
		// @attribute class_label {dummy,1,0,-1}
		// @attribute aspect {dummy,other,teaparty,dems,hcr,gop,conservatives,stupak,liberals,obama}

		if (type.equals("sentiment")) {
			if (classValue == 1) {
				return "positive";
			}
			if (classValue == 2) {
				return "neutral";
			}
			if (classValue == 3) {
				return "negative";
			}
		} else if (type.equals("aspect")) {
			if (classValue == 1) {
				return "other";
			}
			if (classValue == 2) {
				return "teaparty";
			}
			if (classValue == 3) {
				return "dems";
			}
			if (classValue == 4) {
				return "hcr";
			}
			if (classValue == 5) {
				return "gop";
			}
			if (classValue == 6) {
				return "conservatives";
			}
			if (classValue == 7) {
				return "stupak";
			}
			if (classValue == 8) {
				return "liberals";
			}
			if (classValue == 9) {
				return "obama";
			}
		}
		return "unknown label";
	}
}
