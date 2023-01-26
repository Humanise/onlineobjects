package dk.in2isoft.onlineobjects.test.plain;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import dk.in2isoft.commons.http.HeaderUtil;
import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;
import dk.in2isoft.onlineobjects.test.EssentialTests;

@Category(EssentialTests.class)
public class TestHeaderUtil extends AbstractSpringTestCase {
		
	@Test
	public void testValidation() {
		assertEquals("UTF-8", HeaderUtil.getContentTypeEncoding("text/html; charset=UTF-8"));
		assertEquals("ISO-8859-1", HeaderUtil.getContentTypeEncoding("text/html; Charset=ISO-8859-1"));
		assertEquals(null, HeaderUtil.getContentTypeEncoding("text/html"));
		assertEquals(null, HeaderUtil.getContentTypeEncoding(null));
		
		assertEquals("image/jpeg", HeaderUtil.getContentTypesMimeType("image/jpeg"));
		assertEquals(null, HeaderUtil.getContentTypesMimeType(null));
		assertEquals("image/jpeg", HeaderUtil.getContentTypesMimeType(" image/jpeg "));
		assertEquals("text/html", HeaderUtil.getContentTypesMimeType("text/html; charset=UTF-8"));
		assertEquals("text/html", HeaderUtil.getContentTypesMimeType(" text/html ; charset=UTF-8"));
	}
	
}
