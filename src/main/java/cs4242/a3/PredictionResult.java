package cs4242.a3;

public class PredictionResult {
	
	private static final String RAIL = "rail";
	private static final String TAXI = "taxi";
	private static final String BUS = "bus";
	private static final String POSITIVE_SENTIMENT = "positive :)";
	private static final String NEGATIVE_SENTIMENT = "negative >_<";
	private static final String NEUTRAL_SENTIMENT = "neutral :|";
	private static final String NOT_RELEVANT = "not relevant";
	

	private String model;
	private String aspect;
	private String sentiment;

	private PredictionResult() {
		model = "";
		aspect = "";
		sentiment = "";
	}

	public PredictionResult(String model, double classValue) {
		this();
		this.model = model;

		classLabel(classValue);
	}

	private void classLabel(double classValue) {

		// @attribute class
		// {dummy,rail-positive,rail-neutral,rail-negative,
		// taxi-positive,taxi-neutral,taxi-negative,
		// bus-positive,bus-neutral,bus-negative,not-relevant}

		if (classValue == 1) {
			aspect = RAIL;
			sentiment = POSITIVE_SENTIMENT;
		} else if (classValue == 2) {
			aspect = RAIL;
			sentiment = NEUTRAL_SENTIMENT;
		} else if (classValue == 3) {
			aspect = RAIL;
			sentiment = NEGATIVE_SENTIMENT;
		} else if (classValue == 4) {
			aspect = TAXI;
			sentiment = POSITIVE_SENTIMENT;
		} else if (classValue == 5) {
			aspect = TAXI;
			sentiment = NEUTRAL_SENTIMENT;
		} else if (classValue == 6) {
			aspect = TAXI;
			sentiment = NEGATIVE_SENTIMENT;
		}  else if (classValue == 7) {
			aspect = BUS;
			sentiment = POSITIVE_SENTIMENT;
		} else if (classValue == 8) {
			aspect = BUS;
			sentiment = NEUTRAL_SENTIMENT;
		} else if (classValue == 9) {
			aspect = BUS;
			sentiment = NEGATIVE_SENTIMENT;
		} else if (classValue == 10) {
			aspect = NOT_RELEVANT;
			sentiment = NOT_RELEVANT;
		} 
		
	}
}
