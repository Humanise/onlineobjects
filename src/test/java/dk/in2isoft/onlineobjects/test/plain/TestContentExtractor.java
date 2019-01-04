package dk.in2isoft.onlineobjects.test.plain;

import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.Assert;
import org.junit.Test;

import dk.in2isoft.commons.xml.DOM;
import dk.in2isoft.onlineobjects.modules.information.SimpleContentExtractor;
import dk.in2isoft.onlineobjects.modules.information.recognizing.ListRecognizer;
import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;
import nu.xom.Document;
import nu.xom.Element;

public class TestContentExtractor extends AbstractSpringTestCase {
	
	@Test
	public void testSimple() throws MalformedURLException, IOException {
		String xml = "<?xml version='1.0'?>"
				+ "<html xmlns='http://www.w3.org/1999/xhtml'><body>"
				+ "<div role='banner'>I am a banner</div>"
				+ "<div>"
				+ "<h1>This is the title</h1>"
				+ "<p>Aenean eu leo quam. Pellentesque ornare sem lacinia quam venenatis vestibulum. Nulla vitae elit libero, a pharetra augue.</p>"
				+ "<p>Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Maecenas faucibus mollis interdum.</p>"
				+ "</div>"
				+ "<ul><li><a href='#'>Please buy this!</a></li></ul>"
				+ "<p>This is just some junk</p>"
				+ "</body></html>";

		Document document = DOM.parseXOM(xml);

		SimpleContentExtractor extractor = new SimpleContentExtractor();
		
		Document extracted = extractor.extract(document);
		
		String serialized = extracted.toXML();
		String expected = "<?xml version=\"1.0\"?>\n" +
				"<html xmlns=\"http://www.w3.org/1999/xhtml\"><body><div>" + 
				"<h1>This is the title</h1>" + 
				"<p>Aenean eu leo quam. Pellentesque ornare sem lacinia quam venenatis vestibulum. Nulla vitae elit libero, a pharetra augue.</p>" + 
				"<p>Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Maecenas faucibus mollis interdum.</p>" + 
				"</div></body></html>\n";
		Assert.assertEquals(expected , serialized);
	}

	@Test
	public void testListRecognizer() throws MalformedURLException, IOException {
		String xml = "<?xml version='1.0'?><div><h3>References</h3><ul><li><a>test</a></li></ul></div>";
		Document doc = DOM.parseXOM(xml);
		Element ul = doc.getRootElement().getChildElements("ul").get(0);
		ListRecognizer recognizer = new ListRecognizer();
		Assert.assertEquals(0, recognizer.recognize(ul), 0);
	}

	@Test
	public void testListRecognizer2() throws MalformedURLException, IOException {
		String xml = "<?xml version='1.0'?><div><h3>Unrelated</h3><ul><li><a>test</a></li></ul></div>";
		Document doc = DOM.parseXOM(xml);
		Element ul = doc.getRootElement().getChildElements("ul").get(0);
		ListRecognizer recognizer = new ListRecognizer();
		Assert.assertEquals(-1, recognizer.recognize(ul), 0);
	}
}