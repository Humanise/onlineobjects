package dk.in2isoft.onlineobjects.test.plain;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;

import com.google.common.collect.Sets;

import dk.in2isoft.commons.xml.DOM;
import dk.in2isoft.commons.xml.Serializing;
import dk.in2isoft.onlineobjects.test.EssentialTests;
import junit.framework.TestCase;

@Category(EssentialTests.class)
public class TestDOM extends TestCase {

	@Test
	public void testParsing() throws MalformedURLException, IOException {
		String xml = "<?xml version='1.0'?><root/>";
		Document document = DOM.parseDOM(xml);
		Assert.assertEquals(null, document.getXmlEncoding());

		Assert.assertNotNull(document);
		Assert.assertEquals("root", document.getDocumentElement().getNodeName());

		String serialized = Serializing.toString(document);
		Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root/>", serialized);
	}

	@Test
	public void testXOMParsing() throws MalformedURLException, IOException {
		String xml = "<?xml version='1.0'?><root/>";
		nu.xom.Document document = DOM.parseXOM(xml);
		Assert.assertEquals("", document.getBaseURI());

		Assert.assertNotNull(document);
		Assert.assertEquals("root", document.getRootElement().getLocalName());

		String serialized = document.toXML();
		Assert.assertEquals("<?xml version=\"1.0\"?>\n<root />\n", serialized);
	}

	@Test
	public void testParsingInvalid() throws MalformedURLException, IOException {
		Set<String> invalids = Sets.newHashSet(null, "", "<?xml version='1.0'?>", "<?xml version='1.0'?><root></x>");
		for (String xml : invalids) {
			Document document = DOM.parseDOM(xml);
			Assert.assertNull(document);
		}
	}

	@Test
	public void testWildHtml() throws MalformedURLException, IOException {
		Map<String, String> tests = new HashMap<>();
		tests.put("<?xml version='1.0'?><root/>",
				"<?xml version=\"1.0\"?>\n" +
				"<html xmlns=\"http://www.w3.org/1999/xhtml\"><head /><body><root /></body></html>");
		tests.put("<a><h1>Hello</h1></a>",
				"<?xml version=\"1.0\"?>\n" + 
				"<html xmlns=\"http://www.w3.org/1999/xhtml\">" +
				"<head /><body><a><h1>Hello</h1></a></body>" +
				"</html>");
		tests.put("<html><body><a><h1>Hello</h1></a></body></html>",
				"<?xml version=\"1.0\"?>\n" + 
				"<html xmlns=\"http://www.w3.org/1999/xhtml\">" +
				"<head /><body><a><h1>Hello</h1></a></body>" +
				"</html>");
		for (Entry<String, String> test : tests.entrySet()) {
			nu.xom.Document document = DOM.parseWildHhtml(test.getKey());
			Assert.assertEquals(test.getValue(), document.toXML().trim());
		}
	}

}