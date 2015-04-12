package cs4242.a3;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;

public final class StringUtil {
	
	private static final CharMatcher CONTROL_CHARACTERS = CharMatcher.anyOf("\t\b\n\r\f");
	public static final CharMatcher TAB_SEPARATOR = CharMatcher.is('\t');
	

	private StringUtil() {
		// Private constructor, not meant to be instantiated
	}
		
	public static String stripControlCharacters(String s) {
		return CONTROL_CHARACTERS.removeFrom(s);
	}
	
	public static String trim(String s) {
		s = Strings.nullToEmpty(s);
		return CharMatcher.WHITESPACE.trimFrom(s);
	}
}
