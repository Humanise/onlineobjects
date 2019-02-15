package dk.in2isoft.onlineobjects.test.plain;

import org.junit.Test;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.util.ValidationUtil;
import junit.framework.TestCase;

public class TestValidationUtil extends TestCase {
	
	//private static Logger log = LogManager.getLogger(TestLangUtil.class);
	
	
	@Test
	public void testEmailValidation() {
		assertTrue(ValidationUtil.isWellFormedEmail("me@domain.dk"));
		assertTrue(ValidationUtil.isWellFormedEmail("me.and.mynames@sub.domain.com"));
		assertTrue(ValidationUtil.isWellFormedEmail("xx.jb_0m@domain.dk"));

		// Unicode DNS
		assertTrue(ValidationUtil.isWellFormedEmail("xx.jb_0m@a" + Strings.UNICODE_AA + "ira.dk"));
		// Modern TLD
		assertTrue(ValidationUtil.isWellFormedEmail("me.and.mynames@sub.domain.cancerresearch"));

		assertFalse(ValidationUtil.isWellFormedEmail("dora@.com"));

		
		assertFalse(ValidationUtil.isWellFormedEmail("xx.jb_0m@atira.dk       "));
		assertFalse(ValidationUtil.isWellFormedEmail("       xx.jb_0m@atira.dk       "));

		assertFalse(ValidationUtil.isWellFormedEmail("jonasmunk@ma"));
		assertFalse(ValidationUtil.isWellFormedEmail("xx.jb_0m@atira"));
		assertFalse(ValidationUtil.isWellFormedEmail(null));
		assertFalse(ValidationUtil.isWellFormedEmail(""));
		assertFalse(ValidationUtil.isWellFormedEmail("xx"));
		assertFalse(ValidationUtil.isWellFormedEmail("xx.jb_0m@atira.00"));
		assertFalse(ValidationUtil.isWellFormedEmail("xx.jb_0m@atira."));
	}

	@Test
	public void testValidUsername() {
		assertFalse(ValidationUtil.isValidUsername(""));
		assertFalse(ValidationUtil.isValidUsername(null));
		assertFalse(ValidationUtil.isValidUsername(" "));
		assertFalse(ValidationUtil.isValidUsername(" abc"));
		assertFalse(ValidationUtil.isValidUsername("abc123+"));
		assertFalse(ValidationUtil.isValidUsername("jonasmunk@mac.com"));
		assertFalse(ValidationUtil.isValidUsername("a"));
		assertFalse(ValidationUtil.isValidUsername("1"));
		assertFalse(ValidationUtil.isValidUsername("12"));
		assertFalse(ValidationUtil.isValidUsername("a."));
		assertFalse(ValidationUtil.isValidUsername("Abcde"));
		assertFalse(ValidationUtil.isValidUsername("12"));

		assertTrue(ValidationUtil.isValidUsername("1a"));
		assertTrue(ValidationUtil.isValidUsername("ab"));
		assertTrue(ValidationUtil.isValidUsername("abc"));
		assertTrue(ValidationUtil.isValidUsername("abc123"));
	}

	@Test
	public void testValidPassword() {
		assertFalse(ValidationUtil.isValidPassword(""));
		assertFalse(ValidationUtil.isValidPassword(null));
		assertFalse(ValidationUtil.isValidPassword(" "));
		assertFalse(ValidationUtil.isValidPassword("          "));
		assertFalse(ValidationUtil.isValidPassword("\n\n\n\n\n\n\n\n\n\n\n"));
		assertFalse(ValidationUtil.isValidPassword("abcd12_x\n"));
		assertFalse(ValidationUtil.isValidPassword("     abcABC123-+&     "));
		assertFalse(ValidationUtil.isValidPassword(" abcABC123-+&"));
		assertFalse(ValidationUtil.isValidPassword("abcABC123-+& "));
		assertFalse(ValidationUtil.isValidPassword("abBC12+"));
		assertFalse(ValidationUtil.isValidPassword("abcAB C123-+&"));
		assertFalse(ValidationUtil.isValidPassword("asdfghj"));
		assertFalse(ValidationUtil.isValidPassword("--------"));
		assertFalse(ValidationUtil.isValidPassword("aaaaaaaa"));

		assertTrue(ValidationUtil.isValidPassword("abcABC123-+&"));
		assertTrue(ValidationUtil.isValidPassword("new$ecr8p4$s"));
		assertTrue(ValidationUtil.isValidPassword("nVeKeWFgFD3CgveyWcEYUhcY"));
		assertTrue(ValidationUtil.isValidPassword("abcdefgh"));
		assertTrue(ValidationUtil.isValidPassword("12345678"));
		
		// TODO
		
	}
}
