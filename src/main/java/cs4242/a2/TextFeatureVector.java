package cs4242.a2;

import static cs4242.a2.StringUtil.lowerTrim;

import java.util.ArrayList;
import java.util.List;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;

import com.google.common.collect.ImmutableList;

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
	 * @param gender
	 *            the gender to set
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
	 * @param age
	 *            the age to set
	 */
	public TextFeatureVector age(String age) {
		this.age = lowerTrim(age);
		return this;
	}

	public static ArrayList<Attribute> baseHeader(List<String> userIds) {
		
		ArrayList<Attribute> attrs = new ArrayList<Attribute>();
		List<String> values = new ArrayList<String>(userIds.size() + 1);
		values.add("dummy");
		values.addAll(userIds);
		attrs.add(new Attribute("user_id", values));
		values = ImmutableList.<String> builder()
				.add("dummy", "male", "female").build();
		attrs.add(new Attribute("gender", values));
		values = ImmutableList.<String> builder()
				.add("dummy", "18-24", "25-34", "35-49", "50-64", "65-xx")
				.build();
		attrs.add(new Attribute("age", values));
		return attrs;
	}

	public Instance getInstance(Instances header) {
		SparseInstance inst = new SparseInstance(1);
		inst.setDataset(header);
		inst.setValue(header.attribute("user_id"), userId);
		inst.setValue(header.attribute("gender"), gender);
		inst.setValue(header.attribute("age"), age);
		return inst;
	}
	


}
