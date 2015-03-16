package cs4242.a2;

import com.google.gson.annotations.SerializedName;

public class Url {

	@SerializedName("_t")
	private String type;
	@SerializedName("URL")
	private String url;
	@SerializedName("DisplayedURL")
	private String displayedUrl;
	@SerializedName("ExpandedURL")
	private String exandedUrl;
	@SerializedName("Indices")
	private int[] indices;
}
