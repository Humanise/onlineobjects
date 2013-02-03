package dk.in2isoft.onlineobjects.test.plain;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.Assert;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import dk.in2isoft.commons.parsing.HTMLDocument;
import dk.in2isoft.onlineobjects.services.SemanticService;
import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;

public class TestHTMLDocument extends AbstractSpringTestCase {
	
	//private static Logger log = Logger.getLogger(TestHTMLDocument.class);
	
	@Autowired
	private SemanticService semanticService;
		
	@Test
	public void testComplexWikipediaPage() throws MalformedURLException, IOException {
		HTMLDocument doc = new HTMLDocument(getTestFile("language_wikipedia.html").toURI());
		assertEquals("Language - Wikipedia, the free encyclopedia", doc.getTitle());
		String text = doc.getFullText();
		String[] words = semanticService.getWords(text);
		assertEquals(15430,words.length);
	}
	
	@Test
	public void testArticle() throws MalformedURLException, IOException {
		HTMLDocument doc = new HTMLDocument(getTestFile("article.html").toURI());
		//log.info(doc.getText());
		assertEquals("USA hjælper Libanon med bombeundersøgelse", doc.getTitle());
		String text = doc.getText();
		String[] words = semanticService.getWords(text);
		Assert.assertFalse(ArrayUtils.contains(words, "SyrienWashington"));
		Assert.assertFalse(ArrayUtils.contains(words, "THISSHOULDBEEXCLUDED"));
		Assert.assertTrue(ArrayUtils.contains(words, "FBI-folkene"));
		Assert.assertFalse(ArrayUtils.contains(words, "-"));
	}

	public void setSemanticService(SemanticService semanticService) {
		this.semanticService = semanticService;
	}

}