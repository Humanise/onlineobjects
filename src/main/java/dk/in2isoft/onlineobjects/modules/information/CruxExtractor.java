package dk.in2isoft.onlineobjects.modules.information;

import com.chimbori.crux.articles.Article;
import com.chimbori.crux.articles.ArticleExtractor;

import nu.xom.Document;
import okhttp3.HttpUrl;

public class CruxExtractor implements ContentExtractor {

    public String extract(String rawString) {
    	
    	String url = "https://example.com/article.html";
    	HttpUrl httpURL = HttpUrl.Companion.parse(url);
    	Article article = new ArticleExtractor(httpURL, rawString)
    	    .extractMetadata()
    	    .extractContent()
    	    .getArticle();
		org.jsoup.nodes.Document document = article.getDocument();
		return document.outerHtml();
    }

	@Override
	public Document extract(Document document) {
		String extracted = extract(document.toXML());
		return new dk.in2isoft.commons.parsing.HTMLDocument(extracted).getXOMDocument();
	}
}
