package cs4242.a2;

import com.google.gson.annotations.SerializedName;

public class Hashtag {

	@SerializedName("_t")
	private String type;
	@SerializedName("Text")
	private String text;
	@SerializedName("Indices")
	private int[] indices;
	
}
