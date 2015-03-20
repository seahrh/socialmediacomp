package cs4242.a2;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;

public final class StringUtil {
	
	private static final CharMatcher CONTROL_CHARACTERS = CharMatcher.anyOf("\t\b\n\r\f");
	

	private StringUtil() {
		// Private constructor, not meant to be instantiated
	}
	
	public static String lowerTrim(String s) {
		s = Strings.nullToEmpty(s);
		s = s.toLowerCase();
		s = CharMatcher.WHITESPACE.trimFrom(s);
		return s;
	}
	
	public static String stripControlCharacters(String s) {
		return CONTROL_CHARACTERS.removeFrom(s);
	}
}
