package cs4242.a2;

public class TextFeatureVector {

	private String userId;
	private String gender;
	private String age;
	
	private TextFeatureVector() {
		userId = "";
		gender = "";
		age = "";
	}
	
	public TextFeatureVector(String userId) {
		this();
		this.userId = FeatureExtractor.normalize(userId);
	}
	
	public TextFeatureVector(String userId, String gender, String age) {
		this();
		this.userId = FeatureExtractor.normalize(userId);
		gender(gender);
		age(age);
	}

	/**
	 * @return the userId
	 */
	public String userId() {
		return userId;
	}

	/**
	 * @return the gender
	 */
	public String gender() {
		return gender;
	}

	/**
	 * @param gender the gender to set
	 */
	public TextFeatureVector gender(String gender) {
		this.gender = FeatureExtractor.normalize(gender);
		return this;
	}

	/**
	 * @return the age
	 */
	public String age() {
		return age;
	}

	/**
	 * @param age the age to set
	 */
	public TextFeatureVector age(String age) {
		this.age = FeatureExtractor.normalize(age);
		return this;
	}
	
	
}
