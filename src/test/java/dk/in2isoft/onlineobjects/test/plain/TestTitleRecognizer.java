package dk.in2isoft.onlineobjects.test.plain;

import java.util.Map;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.Test;

import dk.in2isoft.commons.xml.DOM;
import dk.in2isoft.onlineobjects.modules.information.recognizing.TitleRecognizer;
import info.debatty.java.stringsimilarity.NormalizedLevenshtein;
import junit.framework.TestCase;
import nu.xom.Document;
import nu.xom.Element;

public class TestTitleRecognizer extends TestCase {

	@Test
	public void testComparison() {
		NormalizedLevenshtein l = new NormalizedLevenshtein();
		String title = "Apple designer Jony Ive explains how ‘teetering towards the absurd‘ helped him make the iPhone | The Independent";
		String rightCandidate = "Apple designer Jony Ive explains how ‘teetering towards the absurd’ helped him make the iPhone";
		String wrongCandidate = "Shape Created with Sketch. Apple designer Jony Ive explains his 'utterly absurd' way of working";
		
		double rightDistance = l.distance(title, rightCandidate);
		double wrongDistance = l.distance(title, wrongCandidate);
		Assert.assertTrue(wrongDistance > rightDistance);
		
	}
	
	@Test
	public void testSimple() {
		String xml = "<?xml version='1.0'?><html>"
				+ "<head></head>"
				+ "<body><h1>My title</h1></body>"
				+ "</html>";
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
		String xml = "<?xml version='1.0'?>"
				+ "<html><head><title>My article</title></head>"
				+ "<body><h1>Website</h1><p>My article</p></body></html>";
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

	@Test
	public void testIndependent() {
		String xml = "<?xml version='1.0'?>"
				+ "<html><head>"
				+ "<title>Apple designer Jony Ive explains how &#x2018;teetering towards the absurd&#x2019; helped him make the iPhone | The Independent</title>"
				+ "<meta name=\"twitter:title\" content=\"Apple designer Jony Ive has explained how &#x2018;teetering towards the absurd&#x2019; helped him make the iPhone\"/></head>"
				+ "<body><div class=\"header\">"
				+ "<h1><span>Apple designer Jony Ive explains how ‘teetering towards the absurd’ helped him make the iPhone</span></h1>" 
				+ "</div><h1 data-title=\"1.0\">"
				+ "<title>Shape</title> Created with Sketch. Apple designer Jony Ive explains his 'utterly absurd' way of working" 
				+ "</h1></body></html>";
		Document document = DOM.parseXOM(xml);
		Assert.assertNotNull(document);

		TitleRecognizer recognizer = new TitleRecognizer();
		Map<Element, Double> recognize = recognizer.recognize(document);
		
		Assert.assertEquals(1, recognize.size());
		
		for (Entry<Element, Double> entry : recognize.entrySet()) {
			if (entry.getValue() == 1) {
				Assert.assertEquals("<h1><span>Apple designer Jony Ive explains how ‘teetering towards the absurd’ helped him make the iPhone</span></h1>", entry.getKey().toXML());
			}
		}
	}
	
	@Test
	public void testNorman() {
		String xml = "<?xml version='1.0'?>"
				+ "<html><head>"
				+ "<title>10 Heuristics for User Interface Design: Article by Jakob Nielsen</title>"
				+ "<meta property=\"og:title\" content=\"10 Heuristics for User Interface Design: Article by Jakob Nielsen\" />" 
				+ "</head>"
				+ "<body>"
				+ "<h1>Browse by Topic and Author</h1>"
				+ "<h1>10 Usability Heuristics for User Interface Design</h1>"
				+ "</body></html>";
		Document document = DOM.parseXOM(xml);
		Assert.assertNotNull(document);

		TitleRecognizer recognizer = new TitleRecognizer();
		Map<Element, Double> recognize = recognizer.recognize(document);
		
		//Assert.assertEquals(1, recognize.size());
		
		for (Entry<Element, Double> entry : recognize.entrySet()) {
			if (entry.getValue() == 1) {
				Assert.assertEquals("<h1>10 Usability Heuristics for User Interface Design</h1>", entry.getKey().toXML());
			}
		}
	}
}