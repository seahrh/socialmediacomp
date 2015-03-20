package cs4242.a2;

import static cs4242.a2.StringUtil.*;
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
		this.userId = lowerTrim(userId);
	}
	
	public TextFeatureVector(String userId, String gender, String age) {
		this(userId);
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
		this.gender = lowerTrim(gender);
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
		this.age = lowerTrim(age);
		return this;
	}
	
	
}
