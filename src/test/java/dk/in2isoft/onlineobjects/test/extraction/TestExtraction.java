package dk.in2isoft.onlineobjects.test.extraction;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.commons.parsing.HTMLDocument;
import dk.in2isoft.onlineobjects.modules.networking.HTMLService;
import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;
import dk.in2isoft.onlineobjects.test.EssentialTests;

@Category(EssentialTests.class)
public class TestExtraction extends AbstractSpringTestCase {
	
	@Autowired
	private HTMLService htmlService;

	private static final Logger log = LogManager.getLogger(TestExtraction.class);
	
	@Test
	public void testArticleExtraction() throws Exception {
		
		File file = getTestFile("html/HTML_Standard.html");
		HTMLDocument document = htmlService.getDocumentSilently(file, Strings.UTF8);
		String text = document.getExtractedText();
		log.info("Extracted: {}", text.substring(0, 1000));

	}


	public void setHtmlService(HTMLService htmlService) {
		this.htmlService = htmlService;
	}
	
}