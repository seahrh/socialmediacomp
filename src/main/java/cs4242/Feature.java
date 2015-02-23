package cs4242;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;

public class Feature {

	/**
	 * These POS don't carry negation semantics
	 */
	private static final Set<String> POS_WITH_NO_NEGATION_CONTEXT = Sets.newHashSet("HT", "URL");
	
	private static final Set<String> MPQA_ADJECTIVE_POS = Sets.newHashSet("JJ", "JJR", "JJS");
	
	private static final Set<String> MPQA_VERB_POS = Sets.newHashSet("VB", "VBD", "VBG", "VBN", "VBP", "VBZ");
	
	private static final Set<String> MPQA_NOUN_POS = Sets.newHashSet("NN", "NNP", "NNPS", "NNS");
	
	private static final Set<String> MPQA_ADVERB_POS = Sets.newHashSet("RB", "RBR", "RBS");
	
	private static final char POS_DELIMITER = '_';

	private String term;
	private boolean negated;
	private String pos;

	private boolean positiveSentiment;
	private boolean negativeSentiment;
	private boolean positiveAndNegativeSentiment;
	private boolean neutralSentiment;
	private boolean stronglySubjective;

	private Feature() {
		term = null;
		negated = false;
		pos = null;
		positiveSentiment = false;
		negativeSentiment = false;
		positiveAndNegativeSentiment = false;
		neutralSentiment = false;
		stronglySubjective = false;
	}
	
	public Feature(String term, String pos) {
		this(term, pos, false);
	}

	public Feature(String term, String pos, boolean negation) {
		this();
		this.term = term;
		this.pos = pos;
		this.negated = negation;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		if (negated) {
			result.append("NOT_");
		}
		result.append(term);
		result.append(POS_DELIMITER);
		result.append(pos);
		return result.toString();
	}

	public String mpqaKey() {
		StringBuffer key = new StringBuffer(term);
		key.append(MpqaClue.POS_DELIMITER);
		
		if (MPQA_ADJECTIVE_POS.contains(pos)) {
			key.append("adj");
		} else if (MPQA_VERB_POS.contains(pos)) {
			key.append("verb");
		} else if (MPQA_NOUN_POS.contains(pos)) {
			key.append("noun");
		} else if (MPQA_ADVERB_POS.contains(pos)) {
			key.append("adverb");
		}
		
		return key.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (negated ? 1231 : 1237);
		result = prime * result + (negativeSentiment ? 1231 : 1237);
		result = prime * result + (neutralSentiment ? 1231 : 1237);
		result = prime * result + ((pos == null) ? 0 : pos.hashCode());
		result = prime * result + (positiveAndNegativeSentiment ? 1231 : 1237);
		result = prime * result + (positiveSentiment ? 1231 : 1237);
		result = prime * result + (stronglySubjective ? 1231 : 1237);
		result = prime * result + ((term == null) ? 0 : term.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Feature)) {
			return false;
		}
		Feature other = (Feature) obj;
		if (negated != other.negated) {
			return false;
		}
		if (negativeSentiment != other.negativeSentiment) {
			return false;
		}
		if (neutralSentiment != other.neutralSentiment) {
			return false;
		}
		if (pos == null) {
			if (other.pos != null) {
				return false;
			}
		} else if (!pos.equals(other.pos)) {
			return false;
		}
		if (positiveAndNegativeSentiment != other.positiveAndNegativeSentiment) {
			return false;
		}
		if (positiveSentiment != other.positiveSentiment) {
			return false;
		}
		if (stronglySubjective != other.stronglySubjective) {
			return false;
		}
		if (term == null) {
			if (other.term != null) {
				return false;
			}
		} else if (!term.equals(other.term)) {
			return false;
		}
		return true;
	}

	/**
	 * @return the term
	 */
	public String term() {
		return term;
	}

	/**
	 * @param term
	 *            the term to set
	 */
	public void term(String term) {
		this.term = term;
	}

	/**
	 * @return the negated
	 */
	public boolean negated() {
		return negated;
	}

	/**
	 * @param negated
	 *            the negated to set
	 */
	public void negated(boolean negated) {
		if (negated && POS_WITH_NO_NEGATION_CONTEXT.contains(pos)) {
			// These POS don't carry negation semantics
			return;
		}
		this.negated = negated;
	}

	/**
	 * @return the pos
	 */
	public String pos() {
		return pos;
	}

	/**
	 * @param pos
	 *            the pos to set
	 */
	public void pos(String pos) {
		this.pos = pos;
	}

	/**
	 * @return the positiveSentiment
	 */
	public boolean positiveSentiment() {
		return positiveSentiment;
	}

	/**
	 * @param positiveSentiment
	 *            the positiveSentiment to set
	 */
	public void positiveSentiment(boolean positiveSentiment) {
		this.positiveSentiment = positiveSentiment;
	}

	/**
	 * @return the negativeSentiment
	 */
	public boolean negativeSentiment() {
		return negativeSentiment;
	}

	/**
	 * @param negativeSentiment
	 *            the negativeSentiment to set
	 */
	public void negativeSentiment(boolean negativeSentiment) {
		this.negativeSentiment = negativeSentiment;
	}

	/**
	 * @return the positiveAndNegativeSentiment
	 */
	public boolean positiveAndNegativeSentiment() {
		return positiveAndNegativeSentiment;
	}

	/**
	 * @param positiveAndNegativeSentiment
	 *            the positiveAndNegativeSentiment to set
	 */
	public void positiveAndNegativeSentiment(
			boolean positiveAndNegativeSentiment) {
		this.positiveAndNegativeSentiment = positiveAndNegativeSentiment;
	}

	/**
	 * @return the neutralSentiment
	 */
	public boolean neutralSentiment() {
		return neutralSentiment;
	}

	/**
	 * @param neutralSentiment
	 *            the neutralSentiment to set
	 */
	public void neutralSentiment(boolean neutralSentiment) {
		this.neutralSentiment = neutralSentiment;
	}

	/**
	 * @return the stronglySubjective
	 */
	public boolean stronglySubjective() {
		return stronglySubjective;
	}

	/**
	 * @param stronglySubjective
	 *            the stronglySubjective to set
	 */
	public void stronglySubjective(boolean stronglySubjective) {
		this.stronglySubjective = stronglySubjective;
	}
	
	public static String toString(List<Feature> features) {
		List<String> result = new ArrayList<String>();
		for (Feature f : features) {
			result.add(f.toString());
		}
		return Joiner.on(' ').join(result);
	}

}
