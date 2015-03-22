package cs4242.a2;

import static cs4242.a2.StringUtil.*;

import java.util.ArrayList;
import java.util.List;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;

public class TextFeatureVector {

	private String userId;
	private String gender;
	private String age;
	private int wordCount;
	private double wordsPerSentence;
	private double questionMarks;
	private double unique;
	private double dictionaryWords;
	private double wordsLongerThan6Letters;
	private double functionWords;
	private double pronouns;
	private double personalPronouns;
	private double firstPersonSingular;
	private double firstPersonPlural;
	private double secondPerson;
	private double thirdPersonSingular;
	private double thirdPersonPlural;
	private double impersonalPronouns;
	private double articles;
	private double verbs;
	private double auxiliaryVerbs;
	private double pastTense;
	private double presentTense;
	private double futureTense;
	private double adverbs;
	private double prepositions;
	private double conjunctions;
	private double negations;
	private double quantifiers;
	private double numbers;
	private double swearWords;
	private double social;
	private double family;
	private double friend;
	private double human;
	private double affective;
	private double positiveEmotion;
	private double negativeEmotion;
	private double anxiety;
	private double anger;
	private double sadness;
	private double cognitive;
	private double insight;
	private double causation;
	private double discrepancy;
	private double tentative;
	private double certainty;
	private double inhibition;
	private double inclusive;
	private double exclusive;
	private double perceptual;
	private double see;
	private double hear;
	private double feel;
	private double biological;
	private double body;
	private double health;
	private double sexual;
	private double ingestion;
	private double relativity;
	private double motion;
	private double space;
	private double time;
	private double work;
	private double achievement;
	private double leisure;
	private double home;
	private double money;
	private double religion;
	private double death;
	private double assent;
	private double nonFluencies;
	private double fillers;

	private TextFeatureVector() {
		userId = "";
		gender = "";
		age = "";
		wordCount = 0;
		wordsPerSentence = 0;
		questionMarks = 0;
		unique = 0;
		dictionaryWords = 0;
		wordsLongerThan6Letters = 0;
		functionWords = 0;
		pronouns = 0;
		personalPronouns = 0;
		firstPersonSingular = 0;
		firstPersonPlural = 0;
		secondPerson = 0;
		thirdPersonSingular = 0;
		thirdPersonPlural = 0;
		impersonalPronouns = 0;
		articles = 0;
		verbs = 0;
		auxiliaryVerbs = 0;
		pastTense = 0;
		presentTense = 0;
		futureTense = 0;
		adverbs = 0;
		prepositions = 0;
		conjunctions = 0;
		negations = 0;
		quantifiers = 0;
		numbers = 0;
		swearWords = 0;
		social = 0;
		family = 0;
		friend = 0;
		human = 0;
		affective = 0;
		positiveEmotion = 0;
		negativeEmotion = 0;
		anxiety = 0;
		anger = 0;
		sadness = 0;
		cognitive = 0;
		insight = 0;
		causation = 0;
		discrepancy = 0;
		tentative = 0;
		certainty = 0;
		inhibition = 0;
		inclusive = 0;
		exclusive = 0;
		perceptual = 0;
		see = 0;
		hear = 0;
		feel = 0;
		biological = 0;
		body = 0;
		health = 0;
		sexual = 0;
		ingestion = 0;
		relativity = 0;
		motion = 0;
		space = 0;
		time = 0;
		work = 0;
		achievement = 0;
		leisure = 0;
		home = 0;
		money = 0;
		religion = 0;
		death = 0;
		assent = 0;
		nonFluencies = 0;
		fillers = 0;
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

	public TextFeatureVector liwc(String liwc) {
		
		List<String> values = Splitter.on('\t').trimResults()
				.splitToList(liwc);
		wordCount = Doubles.tryParse(values.get(1)).intValue();
		wordsPerSentence = Doubles.tryParse(values.get(2));
		questionMarks = Doubles.tryParse(values.get(3));
		unique = Doubles.tryParse(values.get(4));
		dictionaryWords = Doubles.tryParse(values.get(5));
		wordsLongerThan6Letters = Doubles.tryParse(values.get(6));
		functionWords = Doubles.tryParse(values.get(7));
		pronouns = Doubles.tryParse(values.get(8));
		personalPronouns = Doubles.tryParse(values.get(9));
		firstPersonSingular = Doubles.tryParse(values.get(10));
		firstPersonPlural = Doubles.tryParse(values.get(11));
		secondPerson = Doubles.tryParse(values.get(12));
		thirdPersonSingular = Doubles.tryParse(values.get(13));
		thirdPersonPlural = Doubles.tryParse(values.get(14));
		impersonalPronouns = Doubles.tryParse(values.get(15));
		articles = Doubles.tryParse(values.get(16));
		verbs = Doubles.tryParse(values.get(17));
		auxiliaryVerbs = Doubles.tryParse(values.get(18));
		pastTense = Doubles.tryParse(values.get(19));
		presentTense = Doubles.tryParse(values.get(20));
		futureTense = Doubles.tryParse(values.get(21));
		adverbs = Doubles.tryParse(values.get(22));
		prepositions = Doubles.tryParse(values.get(23));
		conjunctions = Doubles.tryParse(values.get(24));
		negations = Doubles.tryParse(values.get(25));
		quantifiers = Doubles.tryParse(values.get(26));
		numbers = Doubles.tryParse(values.get(27));
		swearWords = Doubles.tryParse(values.get(28));
		social = Doubles.tryParse(values.get(29));
		family = Doubles.tryParse(values.get(30));
		friend = Doubles.tryParse(values.get(31));
		human = Doubles.tryParse(values.get(32));
		affective = Doubles.tryParse(values.get(33));
		positiveEmotion = Doubles.tryParse(values.get(34));
		negativeEmotion = Doubles.tryParse(values.get(35));
		anxiety = Doubles.tryParse(values.get(36));
		anger = Doubles.tryParse(values.get(37));
		sadness = Doubles.tryParse(values.get(38));
		cognitive = Doubles.tryParse(values.get(39));
		insight = Doubles.tryParse(values.get(40));
		causation = Doubles.tryParse(values.get(41));
		discrepancy = Doubles.tryParse(values.get(42));
		tentative = Doubles.tryParse(values.get(43));
		certainty = Doubles.tryParse(values.get(44));
		inhibition = Doubles.tryParse(values.get(45));
		inclusive = Doubles.tryParse(values.get(46));
		exclusive = Doubles.tryParse(values.get(47));
		perceptual = Doubles.tryParse(values.get(48));
		see = Doubles.tryParse(values.get(49));
		hear = Doubles.tryParse(values.get(50));
		feel = Doubles.tryParse(values.get(51));
		biological = Doubles.tryParse(values.get(52));
		body = Doubles.tryParse(values.get(53));
		health = Doubles.tryParse(values.get(54));
		sexual = Doubles.tryParse(values.get(55));
		ingestion = Doubles.tryParse(values.get(56));
		relativity = Doubles.tryParse(values.get(57));
		motion = Doubles.tryParse(values.get(58));
		space = Doubles.tryParse(values.get(59));
		time = Doubles.tryParse(values.get(60));
		work = Doubles.tryParse(values.get(61));
		achievement = Doubles.tryParse(values.get(62));
		leisure = Doubles.tryParse(values.get(63));
		home = Doubles.tryParse(values.get(64));
		money = Doubles.tryParse(values.get(65));
		religion = Doubles.tryParse(values.get(66));
		death = Doubles.tryParse(values.get(67));
		assent = Doubles.tryParse(values.get(68));
		nonFluencies = Doubles.tryParse(values.get(69));
		fillers = Doubles.tryParse(values.get(70));
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

	public static ArrayList<Attribute> liwcHeader() {

		final int NUMBER_OF_LIWC_ATTRIBUTES = 70;
		ArrayList<Attribute> attrs = new ArrayList<Attribute>(NUMBER_OF_LIWC_ATTRIBUTES);
		attrs.add(new Attribute("liwc_word_count"));
		attrs.add(new Attribute("liwc_words_per_sentence"));
		attrs.add(new Attribute("liwc_question_marks"));
		attrs.add(new Attribute("liwc_unique"));
		attrs.add(new Attribute("liwc_dictionary_words"));
		attrs.add(new Attribute("liwc_words_longer_than_6_letters"));
		attrs.add(new Attribute("liwc_function_words"));
		attrs.add(new Attribute("liwc_pronouns"));
		attrs.add(new Attribute("liwc_personal_pronouns"));
		attrs.add(new Attribute("liwc_first_person_singular"));
		attrs.add(new Attribute("liwc_first_person_plural"));
		attrs.add(new Attribute("liwc_second_person"));
		attrs.add(new Attribute("liwc_third_person_singular"));
		attrs.add(new Attribute("liwc_third_person_plural"));
		attrs.add(new Attribute("liwc_impersonal_pronouns"));
		attrs.add(new Attribute("liwc_articles"));
		attrs.add(new Attribute("liwc_verbs"));
		attrs.add(new Attribute("liwc_auxiliary_verbs"));
		attrs.add(new Attribute("liwc_past_tense"));
		attrs.add(new Attribute("liwc_present_tense"));
		attrs.add(new Attribute("liwc_future_tense"));
		attrs.add(new Attribute("liwc_adverbs"));
		attrs.add(new Attribute("liwc_prepositions"));
		attrs.add(new Attribute("liwc_conjunctions"));
		attrs.add(new Attribute("liwc_negations"));
		attrs.add(new Attribute("liwc_quantifiers"));
		attrs.add(new Attribute("liwc_numbers"));
		attrs.add(new Attribute("liwc_swear_words"));
		attrs.add(new Attribute("liwc_social"));
		attrs.add(new Attribute("liwc_family"));
		attrs.add(new Attribute("liwc_friend"));
		attrs.add(new Attribute("liwc_human"));
		attrs.add(new Attribute("liwc_affective"));
		attrs.add(new Attribute("liwc_positive_emotion"));
		attrs.add(new Attribute("liwc_negative_emotion"));
		attrs.add(new Attribute("liwc_anxiety"));
		attrs.add(new Attribute("liwc_anger"));
		attrs.add(new Attribute("liwc_sadness"));
		attrs.add(new Attribute("liwc_cognitive"));
		attrs.add(new Attribute("liwc_insight"));
		attrs.add(new Attribute("liwc_causation"));
		attrs.add(new Attribute("liwc_discrepancy"));
		attrs.add(new Attribute("liwc_tentative"));
		attrs.add(new Attribute("liwc_certainty"));
		attrs.add(new Attribute("liwc_inhibition"));
		attrs.add(new Attribute("liwc_inclusive"));
		attrs.add(new Attribute("liwc_exclusive"));
		attrs.add(new Attribute("liwc_perceptual"));
		attrs.add(new Attribute("liwc_see"));
		attrs.add(new Attribute("liwc_hear"));
		attrs.add(new Attribute("liwc_feel"));
		attrs.add(new Attribute("liwc_biological"));
		attrs.add(new Attribute("liwc_body"));
		attrs.add(new Attribute("liwc_health"));
		attrs.add(new Attribute("liwc_sexual"));
		attrs.add(new Attribute("liwc_ingestion"));
		attrs.add(new Attribute("liwc_relativity"));
		attrs.add(new Attribute("liwc_motion"));
		attrs.add(new Attribute("liwc_space"));
		attrs.add(new Attribute("liwc_time"));
		attrs.add(new Attribute("liwc_work"));
		attrs.add(new Attribute("liwc_achievement"));
		attrs.add(new Attribute("liwc_leisure"));
		attrs.add(new Attribute("liwc_home"));
		attrs.add(new Attribute("liwc_money"));
		attrs.add(new Attribute("liwc_religion"));
		attrs.add(new Attribute("liwc_death"));
		attrs.add(new Attribute("liwc_assent"));
		attrs.add(new Attribute("liwc_non_fluencies"));
		attrs.add(new Attribute("liwc_fillers"));
		return attrs;
	}

	public Instance getInstance(Instances header) {
		SparseInstance inst = new SparseInstance(1);
		inst.setDataset(header);
		inst.setValue(header.attribute("user_id"), userId);
		inst.setValue(header.attribute("gender"), gender);
		inst.setValue(header.attribute("age"), age);
		
		setLiwcAttributes(inst);
		return inst;
	}

	private Instance setLiwcAttributes(Instance inst) {
		Instances header = inst.dataset();
		Attribute attr = null;
		attr = header.attribute("liwc_word_count");
		if (attr != null) {
			inst.setValue(attr, wordCount);
		}
		attr = header.attribute("liwc_words_per_sentence");
		if (attr != null) {
			inst.setValue(attr, wordsPerSentence);
		}
		attr = header.attribute("liwc_question_marks");
		if (attr != null) {
			inst.setValue(attr, questionMarks);
		}
		attr = header.attribute("liwc_unique");
		if (attr != null) {
			inst.setValue(attr, unique);
		}
		attr = header.attribute("liwc_dictionary_words");
		if (attr != null) {
			inst.setValue(attr, dictionaryWords);
		}
		attr = header.attribute("liwc_words_longer_than_6_letters");
		if (attr != null) {
			inst.setValue(attr, wordsLongerThan6Letters);
		}
		attr = header.attribute("liwc_function_words");
		if (attr != null) {
			inst.setValue(attr, functionWords);
		}
		attr = header.attribute("liwc_pronouns");
		if (attr != null) {
			inst.setValue(attr, pronouns);
		}
		attr = header.attribute("liwc_personal_pronouns");
		if (attr != null) {
			inst.setValue(attr, personalPronouns);
		}
		attr = header.attribute("liwc_first_person_singular");
		if (attr != null) {
			inst.setValue(attr, firstPersonSingular);
		}
		attr = header.attribute("liwc_first_person_plural");
		if (attr != null) {
			inst.setValue(attr, firstPersonPlural);
		}
		attr = header.attribute("liwc_second_person");
		if (attr != null) {
			inst.setValue(attr, secondPerson);
		}
		attr = header.attribute("liwc_third_person_singular");
		if (attr != null) {
			inst.setValue(attr, thirdPersonSingular);
		}
		attr = header.attribute("liwc_third_person_plural");
		if (attr != null) {
			inst.setValue(attr, thirdPersonPlural);
		}
		attr = header.attribute("liwc_impersonal_pronouns");
		if (attr != null) {
			inst.setValue(attr, impersonalPronouns);
		}
		attr = header.attribute("liwc_articles");
		if (attr != null) {
			inst.setValue(attr, articles);
		}
		attr = header.attribute("liwc_verbs");
		if (attr != null) {
			inst.setValue(attr, verbs);
		}
		attr = header.attribute("liwc_auxiliary_verbs");
		if (attr != null) {
			inst.setValue(attr, auxiliaryVerbs);
		}
		attr = header.attribute("liwc_past_tense");
		if (attr != null) {
			inst.setValue(attr, pastTense);
		}
		attr = header.attribute("liwc_present_tense");
		if (attr != null) {
			inst.setValue(attr, presentTense);
		}
		attr = header.attribute("liwc_future_tense");
		if (attr != null) {
			inst.setValue(attr, futureTense);
		}
		attr = header.attribute("liwc_adverbs");
		if (attr != null) {
			inst.setValue(attr, adverbs);
		}
		attr = header.attribute("liwc_prepositions");
		if (attr != null) {
			inst.setValue(attr, prepositions);
		}
		attr = header.attribute("liwc_conjunctions");
		if (attr != null) {
			inst.setValue(attr, conjunctions);
		}
		attr = header.attribute("liwc_negations");
		if (attr != null) {
			inst.setValue(attr, negations);
		}
		attr = header.attribute("liwc_quantifiers");
		if (attr != null) {
			inst.setValue(attr, quantifiers);
		}
		attr = header.attribute("liwc_numbers");
		if (attr != null) {
			inst.setValue(attr, numbers);
		}
		attr = header.attribute("liwc_swear_words");
		if (attr != null) {
			inst.setValue(attr, swearWords);
		}
		attr = header.attribute("liwc_social");
		if (attr != null) {
			inst.setValue(attr, social);
		}
		attr = header.attribute("liwc_family");
		if (attr != null) {
			inst.setValue(attr, family);
		}
		attr = header.attribute("liwc_friend");
		if (attr != null) {
			inst.setValue(attr, friend);
		}
		attr = header.attribute("liwc_human");
		if (attr != null) {
			inst.setValue(attr, human);
		}
		attr = header.attribute("liwc_affective");
		if (attr != null) {
			inst.setValue(attr, affective);
		}
		attr = header.attribute("liwc_positive_emotion");
		if (attr != null) {
			inst.setValue(attr, positiveEmotion);
		}
		attr = header.attribute("liwc_negative_emotion");
		if (attr != null) {
			inst.setValue(attr, negativeEmotion);
		}
		attr = header.attribute("liwc_anxiety");
		if (attr != null) {
			inst.setValue(attr, anxiety);
		}
		attr = header.attribute("liwc_anger");
		if (attr != null) {
			inst.setValue(attr, anger);
		}
		attr = header.attribute("liwc_sadness");
		if (attr != null) {
			inst.setValue(attr, sadness);
		}
		attr = header.attribute("liwc_cognitive");
		if (attr != null) {
			inst.setValue(attr, cognitive);
		}
		attr = header.attribute("liwc_insight");
		if (attr != null) {
			inst.setValue(attr, insight);
		}
		attr = header.attribute("liwc_causation");
		if (attr != null) {
			inst.setValue(attr, causation);
		}
		attr = header.attribute("liwc_discrepancy");
		if (attr != null) {
			inst.setValue(attr, discrepancy);
		}
		attr = header.attribute("liwc_tentative");
		if (attr != null) {
			inst.setValue(attr, tentative);
		}
		attr = header.attribute("liwc_certainty");
		if (attr != null) {
			inst.setValue(attr, certainty);
		}
		attr = header.attribute("liwc_inhibition");
		if (attr != null) {
			inst.setValue(attr, inhibition);
		}
		attr = header.attribute("liwc_inclusive");
		if (attr != null) {
			inst.setValue(attr, inclusive);
		}
		attr = header.attribute("liwc_exclusive");
		if (attr != null) {
			inst.setValue(attr, exclusive);
		}
		attr = header.attribute("liwc_perceptual");
		if (attr != null) {
			inst.setValue(attr, perceptual);
		}
		attr = header.attribute("liwc_see");
		if (attr != null) {
			inst.setValue(attr, see);
		}
		attr = header.attribute("liwc_hear");
		if (attr != null) {
			inst.setValue(attr, hear);
		}
		attr = header.attribute("liwc_feel");
		if (attr != null) {
			inst.setValue(attr, feel);
		}
		attr = header.attribute("liwc_biological");
		if (attr != null) {
			inst.setValue(attr, biological);
		}
		attr = header.attribute("liwc_body");
		if (attr != null) {
			inst.setValue(attr, body);
		}
		attr = header.attribute("liwc_health");
		if (attr != null) {
			inst.setValue(attr, health);
		}
		attr = header.attribute("liwc_sexual");
		if (attr != null) {
			inst.setValue(attr, sexual);
		}
		attr = header.attribute("liwc_ingestion");
		if (attr != null) {
			inst.setValue(attr, ingestion);
		}
		attr = header.attribute("liwc_relativity");
		if (attr != null) {
			inst.setValue(attr, relativity);
		}
		attr = header.attribute("liwc_motion");
		if (attr != null) {
			inst.setValue(attr, motion);
		}
		attr = header.attribute("liwc_space");
		if (attr != null) {
			inst.setValue(attr, space);
		}
		attr = header.attribute("liwc_time");
		if (attr != null) {
			inst.setValue(attr, time);
		}
		attr = header.attribute("liwc_work");
		if (attr != null) {
			inst.setValue(attr, work);
		}
		attr = header.attribute("liwc_achievement");
		if (attr != null) {
			inst.setValue(attr, achievement);
		}
		attr = header.attribute("liwc_leisure");
		if (attr != null) {
			inst.setValue(attr, leisure);
		}
		attr = header.attribute("liwc_home");
		if (attr != null) {
			inst.setValue(attr, home);
		}
		attr = header.attribute("liwc_money");
		if (attr != null) {
			inst.setValue(attr, money);
		}
		attr = header.attribute("liwc_religion");
		if (attr != null) {
			inst.setValue(attr, religion);
		}
		attr = header.attribute("liwc_death");
		if (attr != null) {
			inst.setValue(attr, death);
		}
		attr = header.attribute("liwc_assent");
		if (attr != null) {
			inst.setValue(attr, assent);
		}
		attr = header.attribute("liwc_non_fluencies");
		if (attr != null) {
			inst.setValue(attr, nonFluencies);
		}
		attr = header.attribute("liwc_fillers");
		if (attr != null) {
			inst.setValue(attr, fillers);
		}
		
		return inst;
	}
}
