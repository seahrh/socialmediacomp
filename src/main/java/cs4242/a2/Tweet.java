package cs4242.a2;

import java.util.List;

public class Tweet {

	private String userId;
	private String text;
	private List<Hashtag> hashtags;
	private List<Url> urls;
	
	public String userId() {
		return userId;
	}
}
