package cs4242.a3;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;

public final class StringUtil {
	
	// Follow Wikipedia's definition of punctutation
	// @url http://en.wikipedia.org/wiki/Punctuation
	
	public static final String SYMBOL_CHARACTERS = "><*%&=^~|@#$_\\";
	public static final String PUNCTUATION_CHARACTERS = "[](){}:,`.!?\"';/-";
	public static final CharMatcher SYMBOLS = CharMatcher.anyOf(SYMBOL_CHARACTERS);
	public static final CharMatcher PUNCTUATION = CharMatcher.anyOf(PUNCTUATION_CHARACTERS);
	public static final CharMatcher CONTROL_CHARACTERS = CharMatcher.anyOf("\t\b\n\r\f");
	public static final CharMatcher TAB = CharMatcher.is('\t');
	public static final CharMatcher HEX = CharMatcher.is('#');
	public static final CharMatcher AMPERSAND = CharMatcher.is('@');
	public static final CharMatcher UNDERSCORE = CharMatcher.is('_');
	

	private StringUtil() {
		// Private constructor, not meant to be instantiated
	}
	
	public static String trim(String s) {
		s = Strings.nullToEmpty(s);
		return CharMatcher.WHITESPACE.trimFrom(s);
	}
	
	public static int countUpper(String s) {
		s = Strings.nullToEmpty(s);
		int count = 0;
		char c;
		for (int i = 0; i < s.length(); i++) {
			c = s.charAt(i);
			if (CharMatcher.JAVA_UPPER_CASE.matches(c)) {
				count++;
			}
		}
		return count;
	}
	
	public static int countPunctuationSymbol(String s) {
		s = Strings.nullToEmpty(s);
		int count = 0;
		char c;
		for (int i = 0; i < s.length(); i++) {
			c = s.charAt(i);
			if (PUNCTUATION.or(SYMBOLS).matches(c)) {
				count++;
			}
		}
		return count;
	}
}
