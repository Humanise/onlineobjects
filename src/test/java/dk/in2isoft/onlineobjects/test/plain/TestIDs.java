package dk.in2isoft.onlineobjects.test.plain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hashids.Hashids;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import dk.in2isoft.onlineobjects.test.EssentialTests;
import junit.framework.TestCase;

@Category(EssentialTests.class)
public class TestIDs extends TestCase {
	
	private static Logger log = LogManager.getLogger(TestIDs.class);
	
	@Test
	public void testRandomStringGenerator() {
		Hashids hashids = new Hashids("this is my salt", 8);
		long id = 12345L;
		String hash = hashids.encode(id);
		assertEquals("B0NkK9A5", hash);
		log.info(hash);
		long[] back = hashids.decode(hash);
		
		assertEquals(id, back[0]);
	}
}
