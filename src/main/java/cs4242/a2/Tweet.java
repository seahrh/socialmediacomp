package cs4242.a2;

import java.util.List;
import static cs4242.a2.StringUtil.*;

public class Tweet {

	private String userId;
	private String text;
	private List<Hashtag> hashtags;
	private List<Url> urls;
	
	public String userId() {
		return userId;
	}
	
	public String text() {
		return text;
	}
	
	public Tweet text(String text) {
		this.text = stripControlCharacters(text);
		return this;
	}
}
