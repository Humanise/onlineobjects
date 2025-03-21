package dk.in2isoft.onlineobjects.test.plain;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.google.common.collect.Maps;

import dk.in2isoft.commons.xml.DOM;
import dk.in2isoft.commons.xml.DocumentCleaner;
import dk.in2isoft.onlineobjects.test.EssentialTests;

@Category(EssentialTests.class)
public class TestDocumentCleaner extends TestCase {

	@Test
	public void testCleaning() throws MalformedURLException, IOException {
		String xml = "<?xml version='1.0'?>"
				+ "<html xmlns=\"http://www.w3.org/1999/xhtml\"><body>"
				+ "<!-- Hello -->"
				+ "<script>alert('I am evil')</script>"
				+ "<noscript>I am hidden, or am I?</noscript>"
				+ "<style type=\"text/css\">body {background: red;}</style>"
				+ "<div><div><h1 class='header'>Title</h1></div></div><div/>"
				+ "<p><a href='http://www.somewhere.com/' onclick='doSomethingNasty()'><span>This is a link</span></a></p>"
				+ "<p> </p>"
				+ "<ul><li> </li><li><a href='#'></a></li><li>I can stay</li></ul>"
				+ "<h2><span class='hey'>hep</span></h2>"
				+ "</body></html>";

		String expected = "<?xml version=\"1.0\"?>\n"
				+ "<html xmlns=\"http://www.w3.org/1999/xhtml\"><body>"
				+ "<h1>Title</h1>"
				+ "<p><a href=\"http://www.somewhere.com/\">This is a link</a></p>"
				+ "<ul><li>I can stay</li></ul>"
				+ "<h2>hep</h2>"
				+ "</body></html>\n";

		nu.xom.Document document = DOM.parseXOM(xml);

		DocumentCleaner cleaner = new DocumentCleaner();
		cleaner.clean(document);

		String serialized = document.toXML();
		Assert.assertEquals(expected, serialized);
	}

	@Test
	public void testMultiple() throws MalformedURLException, IOException {
		Map<String,String> tests = Maps.newHashMap();
		{
			String xml = "<div><div><div><div><div><h1>Hep hey</h1>hippie</div></div></div></div></div>";
			String expected = "<h1>Hep hey</h1>hippie";
			tests.put(xml, expected);
		}
		{
			String xml = "<h1>Remove empty tags</h1><p><strong><em></em></strong></p><h3/><hr/><br/>";
			String expected = "<h1>Remove empty tags</h1><hr /><br />";
			tests.put(xml, expected);
		}
		{
			String xml = "<h2><span>Remove spans</span></h2>";
			String expected = "<h2>Remove spans</h2>";
			tests.put(xml, expected);
		}
		{
			String xml = "";
			String expected = "";
			tests.put(xml, expected);
		}
		{
			String xml = "<div></div>";
			String expected = "";
			tests.put(xml, expected);
		}
		{
			String xml = "<p>help researchers determine<strong> </strong>what kinds of environments</p>";
			String expected = "<p>help researchers determine what kinds of environments</p>";
			tests.put(xml, expected);
		}
		
		
		tests.forEach((dirty, expected) -> {
			
			dirty = "<?xml version='1.0'?>"
					+ "<html xmlns=\"http://www.w3.org/1999/xhtml\"><body>"
					+ dirty
					+ "</body></html>";
			expected = "<?xml version=\"1.0\"?>\n"
					+ "<html xmlns=\"http://www.w3.org/1999/xhtml\">"
					+ (expected.isEmpty() ? "<body />" : "<body>" + expected + "</body>")
					+ "</html>\n";
			
			nu.xom.Document document = DOM.parseXOM(dirty);

			DocumentCleaner cleaner = new DocumentCleaner();
			cleaner.clean(document);

			String serialized = document.toXML();
			Assert.assertEquals(expected, serialized);
		});
	}
}