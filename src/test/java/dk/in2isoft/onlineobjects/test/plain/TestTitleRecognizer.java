package dk.in2isoft.onlineobjects.test.plain;

import java.util.Map;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.Test;

import dk.in2isoft.commons.xml.DOM;
import dk.in2isoft.onlineobjects.modules.information.recognizing.TitleRecognizer;
import junit.framework.TestCase;
import nu.xom.Document;
import nu.xom.Element;

public class TestTitleRecognizer extends TestCase {

	@Test
	public void testSimple() {
		String xml = "<?xml version='1.0'?><html><head></head><body><h1>My title</h1></body></html>";
		Document document = DOM.parseXOM(xml);
		Assert.assertNotNull(document);

		TitleRecognizer recognizer = new TitleRecognizer();
		Map<Element, Double> recognize = recognizer.recognize(document);
		
		Assert.assertEquals(1, recognize.size());
		
		Entry<Element, Double> first = recognize.entrySet().iterator().next();
		Assert.assertEquals("<h1>My title</h1>", first.getKey().toXML());
	}

	@Test
	public void testNonH1() {
		String xml = "<?xml version='1.0'?><html><head><title>My article</title></head><body><h1>Website</h1><p>My article</p></body></html>";
		Document document = DOM.parseXOM(xml);
		Assert.assertNotNull(document);

		TitleRecognizer recognizer = new TitleRecognizer();
		Map<Element, Double> recognize = recognizer.recognize(document);
		
		Assert.assertEquals(2, recognize.size());
		
		for (Entry<Element, Double> entry : recognize.entrySet()) {
			if (entry.getValue() == -1) {
				Assert.assertEquals("<h1>Website</h1>", entry.getKey().toXML());
			}
			if (entry.getValue() == 1) {
				Assert.assertEquals("<p>My article</p>", entry.getKey().toXML());
			}
		}
	}

}