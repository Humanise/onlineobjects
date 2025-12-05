package dk.in2isoft.onlineobjects.apps.knowledge.perspective;

import java.util.List;

import dk.in2isoft.in2igui.data.ItemData;
import dk.in2isoft.onlineobjects.model.InternetAddress;
import dk.in2isoft.onlineobjects.ui.data.Option;
import dk.in2isoft.onlineobjects.ui.data.SimilarityPerspective;

public class InternetAddressViewPerspective implements CategorizableViewPerspective, ViewPerspectiveWithTags {

	private long id;
	private String type = InternetAddress.class.getSimpleName();
	private String title;
	private String url;
	private String urlText;

	private String info;
	private String header;

	private String formatted;
	private String text;

	private boolean inbox;
	private boolean favorite;

	private List<QuotePerspective> quotes;
	private List<QuotePerspective> hypotheses;
	private List<Option> words;
	private List<Option> tags;
	private List<ItemData> authors;

	private List<SimilarityPerspective> similar;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getFormatted() {
		return formatted;
	}

	public void setFormatted(String rendering) {
		this.formatted = rendering;
	}

	public List<Option> getWords() {
		return words;
	}

	public void setWords(List<Option> words) {
		this.words = words;
	}

	public List<Option> getTags() {
		return tags;
	}

	public void setTags(List<Option> tags) {
		this.tags = tags;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public List<QuotePerspective> getQuotes() {
		return quotes;
	}

	public void setQuotes(List<QuotePerspective> quotes) {
		this.quotes = quotes;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Boolean isInbox() {
		return inbox;
	}

	public void setInbox(Boolean inbox) {
		this.inbox = inbox;
	}

	public Boolean isFavorite() {
		return favorite;
	}

	public void setFavorite(Boolean favorite) {
		this.favorite = favorite;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public List<ItemData> getAuthors() {
		return authors;
	}

	public void setAuthors(List<ItemData> authors) {
		this.authors = authors;
	}

	public List<QuotePerspective> getHypotheses() {
		return hypotheses;
	}

	public void setHypotheses(List<QuotePerspective> hypotheses) {
		this.hypotheses = hypotheses;
	}

	public String getUrlText() {
		return urlText;
	}

	public void setUrlText(String urlText) {
		this.urlText = urlText;
	}

	public List<SimilarityPerspective> getSimilar() {
		return similar;
	}

	public void setSimilar(List<SimilarityPerspective> similar) {
		this.similar = similar;
	}
}
