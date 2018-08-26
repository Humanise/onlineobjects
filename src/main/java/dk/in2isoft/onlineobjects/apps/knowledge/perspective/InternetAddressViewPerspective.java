package dk.in2isoft.onlineobjects.apps.knowledge.perspective;

import java.util.List;

import dk.in2isoft.in2igui.data.ItemData;
import dk.in2isoft.onlineobjects.ui.data.Option;
import dk.in2isoft.onlineobjects.ui.data.SimilarityPerspective;

public class InternetAddressViewPerspective implements CategorizableViewPerspective {

	private long id;
	private String title;
	private String url;
	private String urlText;

	private String info;
	private String header;

	private String formatted;
	private String text;

	private boolean inbox;
	private boolean favorite;

	private List<StatementPerspective> quotes;
	private List<StatementPerspective> hypotheses;
	private List<Option> words;
	private List<ItemData> authors;

	private List<SimilarityPerspective> similar;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public List<StatementPerspective> getQuotes() {
		return quotes;
	}

	public void setQuotes(List<StatementPerspective> quotes) {
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

	public boolean isInbox() {
		return inbox;
	}

	public void setInbox(boolean inbox) {
		this.inbox = inbox;
	}

	public boolean isFavorite() {
		return favorite;
	}

	public void setFavorite(boolean favorite) {
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

	public List<StatementPerspective> getHypotheses() {
		return hypotheses;
	}

	public void setHypotheses(List<StatementPerspective> hypotheses) {
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
