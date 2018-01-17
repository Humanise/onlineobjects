package dk.in2isoft.onlineobjects.modules.information;

import com.chimbori.crux.articles.Article;
import com.chimbori.crux.articles.ArticleExtractor;

import nu.xom.Document;

public class CruxExtractor implements ContentExtractor {

    public String extract(String rawString) {
    	
    	String url = "https://example.com/article.html";

    	Article article = ArticleExtractor.with(url, rawString)
    	    .extractMetadata()
    	    .extractContent()
    	    .article();
		org.jsoup.nodes.Document document = article.document;
		return document.outerHtml();
    }

	@Override
	public Document extract(Document document) {
		String extracted = extract(document.toXML());
		return new dk.in2isoft.commons.parsing.HTMLDocument(extracted).getXOMDocument();
	}
}
