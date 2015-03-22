package cs4242.a2;

import static cs4242.a2.StringUtil.lowerTrim;
import static cs4242.a2.StringUtil.trim;
import edu.stanford.nlp.process.Morphology;

public class Word {

	private static final char POS_SEPARATOR = '_';
	private static final Morphology LEMMATIZER = new Morphology();
	private String term;
	private String pos;
	
	private Word() {
		term = null;
		pos = null;
	}
	
	public Word(String term, String pos) {
		this.term = LEMMATIZER.stem(lowerTrim(term));
		this.pos = trim(pos);
	}
	
	public String term() {
		return term;
	}
	
	public String pos() {
		return pos;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pos == null) ? 0 : pos.hashCode());
		result = prime * result + ((term == null) ? 0 : term.hashCode());
		return result;
	}

	/* (non-Javadoc)
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
		if (!(obj instanceof Word)) {
			return false;
		}
		Word other = (Word) obj;
		if (pos == null) {
			if (other.pos != null) {
				return false;
			}
		} else if (!pos.equals(other.pos)) {
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
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(term);
		sb.append(POS_SEPARATOR);
		sb.append(pos);
		return sb.toString();
	}
}
