package dk.in2isoft.onlineobjects.test.plain;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import dk.in2isoft.commons.xml.DOM;
import dk.in2isoft.commons.xml.DocumentToText;
import dk.in2isoft.onlineobjects.test.EssentialTests;
import junit.framework.TestCase;

@Category(EssentialTests.class)
public class TestDocumentToText extends TestCase {

	@Test
	public void testIt() {
		String xml = "<?xml version='1.0'?><html>" +
				"<head><title>Hello world</title></head>" +
				"<!-- Hep hey -->" +
				"<body>" + 
				"<style>h1 {color: red;}</style>" +
				"<div><h1>Lorem  <span>ipsum</span></h1></div>" + 
				"<p>Abe</p><p></p>" + 
				"<div>Hest<br/>Hund</div>" +
				"Flodhest"+
				"<script>doSomething();</script>" +
				"</body>" + 
				"</html>";
		nu.xom.Document document = DOM.parseXOM(xml);
		String text = new DocumentToText().getText(document);
		
		Assert.assertEquals("Lorem ipsum\n\nAbe\n\nHest\nHund\nFlodhest", text);
	}

	@Test
	public void testWhiteSpace() {
		String xml = "<?xml version='1.0'?><html>" +
				"<head><title>Hello world</title></head>" +
				"<body>" + 
				"<p><br/><br/>  <br/><br/>    <br/><br/>  <br/><br/></p>"+
				"<p><br/><br/>    This is\n\n\na test  <br/>   <br/> <br/>one more line   <br/></p>" + 
				"</body>" +
				"</html>";
		nu.xom.Document document = DOM.parseXOM(xml);
		String text = new DocumentToText().getText(document);
		
		Assert.assertEquals("This is a test\n\none more line", text);
	}
	@Test
	public void testTables() {
		String xml = "<?xml version='1.0'?><html>" +
				"<head><title>Hello world</title></head>" +
				"<body>" + 
				"<table>\n" +
				"<tr><td>A</td>\n<td>B\n</td><td>C</td>\n</tr>" + 
				"</table>" +
				"</body>" +
				"</html>";
		nu.xom.Document document = DOM.parseXOM(xml);
		String text = new DocumentToText().getText(document);
		
		Assert.assertEquals("A B C", text);
	}
}
