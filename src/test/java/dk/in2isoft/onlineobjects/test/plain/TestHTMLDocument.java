package dk.in2isoft.onlineobjects.test.plain;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.function.Consumer;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.time.StopWatch;
import org.junit.Assert;
import org.junit.Test;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.commons.parsing.HTMLDocument;
import dk.in2isoft.commons.xml.DocumentCleaner;
import dk.in2isoft.onlineobjects.modules.information.ContentExtractor;
import dk.in2isoft.onlineobjects.modules.information.SimpleContentExtractor;
import dk.in2isoft.onlineobjects.modules.networking.HTMLService;
import dk.in2isoft.onlineobjects.services.SemanticService;
import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;
import nu.xom.Document;
import nu.xom.Node;

public class TestHTMLDocument extends AbstractSpringTestCase {
	
	private static Logger log = LogManager.getLogger(TestHTMLDocument.class);
	
	@Autowired
	private HTMLService htmlService;

	@Autowired
	private SemanticService semanticService;
		
	@Test
	public void testComplexWikipediaPage() throws MalformedURLException, IOException {
		HTMLDocument doc = htmlService.getDocumentSilently(getTestFile("language_wikipedia.html").toURI());
		assertEquals("Language - Wikipedia, the free encyclopedia", doc.getTitle());
		String text = doc.getFullText();
		String[] words = semanticService.getWords(text);
		assertEquals(15402,words.length);
	}
	
	@Test
	public void testTravel() throws MalformedURLException, IOException {
		StopWatch watch = new StopWatch();
		watch.start();
		watch.split();
		HTMLDocument doc = htmlService.getDocumentSilently(getTestFile("html/HTML_Standard.html").toURI());
		watch.split();
		log.info("Loaded: " + watch.getSplitTime());
		assertEquals("HTML Standard", doc.getTitle());

		Document xom = doc.getXOMDocument();
		watch.split();
		log.info("Get XOMDocument: " + watch.getSplitTime());
		
		travel(xom, node -> {
			//log.info(node.getClass().getSimpleName());
		});
		log.info("Finished traveling: " + watch.getSplitTime());
	}
	
	private void travel(Node node, Consumer<Node> consumer) {
		int count = node.getChildCount();
		for (int i = 0; i < count; i++) {
			Node child = node.getChild(i);
			consumer.accept(child);
			travel(child, consumer);
		}
	}

	@Test
	public void testEnormousPage() throws MalformedURLException, IOException {
		StopWatch watch = new StopWatch();
		watch.start();
		watch.split();
		String path = "html/HTML_Standard.html";
		path = "html/ww11.apirocks.com.html";
		HTMLDocument doc = htmlService.getDocumentSilently(getTestFile(path).toURI());
		watch.split();
		log.info("Loaded: " + watch.getSplitTime());
		//assertEquals("HTML Standard", doc.getTitle());

		Document xom = doc.getXOMDocument();
		watch.split();
		log.info("Get XOMDocument: " + watch.getSplitTime());

		String text = doc.getFullText();
		watch.split();
		log.info("Get full text: " + watch.getSplitTime());

		String[] words = semanticService.getWords(text);
		//assertEquals(513110,words.length);
		watch.split();
		log.info("Get words: " + watch.getSplitTime());


		ContentExtractor extractor = new SimpleContentExtractor();
		Document extracted = extractor.extract(xom);
		watch.split();
		log.info("Extracted: " + watch.getSplitTime());

		DocumentCleaner cleaner = new DocumentCleaner();
		cleaner.setUrl(doc.getOriginalUrl());
		cleaner.clean(extracted);
		watch.split();
		log.info("Cleaned: " + watch.getSplitTime());
	}

	@Test
	public void testArticle() throws Exception {
		HTMLDocument doc = htmlService.getDocumentSilently(getTestFile("article.html").toURI());
		//log.info(doc.getText());
		assertEquals("USA hjælper Libanon med bombeundersøgelse", doc.getTitle());
		String text = doc.getText();
		String[] words = semanticService.getWords(text);
		Assert.assertFalse(ArrayUtils.contains(words, "SyrienWashington"));
		Assert.assertFalse(ArrayUtils.contains(words, "THISSHOULDBEEXCLUDED"));
		Assert.assertTrue(ArrayUtils.contains(words, "FBI-folkene"));
		Assert.assertFalse(ArrayUtils.contains(words, "-"));
	}

	@Test
	public void testArticleExtraction() throws Exception {
		File folder = new File(getResourcesDir(),"articles");
		Assert.assertTrue(folder.isDirectory());
		File[] htmlFiles = folder.listFiles((FileFilter) pathname -> {
			return pathname.getName().endsWith("html");
		});
		DocumentCleaner cleaner = new DocumentCleaner();
		
		for (File file : htmlFiles) {
			HTMLDocument doc = htmlService.getDocumentSilently(file, Strings.UTF8);
			Assert.assertNotNull(doc);
			{
				File out = new File(folder,file.getName()+".extracted.htm");
				try (FileWriter w = new FileWriter(out)) {
					Document document = doc.getXOMDocument();
					SimpleContentExtractor x = new SimpleContentExtractor();
					Document extracted = x.extract(document);
					cleaner.clean(extracted);
					w.append(extracted.toXML());
				}
			}
		}
	}

	// Wiring...

	public void setSemanticService(SemanticService semanticService) {
		this.semanticService = semanticService;
	}

	public void setHtmlService(HTMLService htmlService) {
		this.htmlService = htmlService;
	}
}