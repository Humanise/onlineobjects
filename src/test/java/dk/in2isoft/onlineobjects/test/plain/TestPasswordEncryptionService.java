package dk.in2isoft.onlineobjects.test.plain;

import static org.junit.Assert.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import dk.in2isoft.onlineobjects.core.PasswordEncryptionService;
import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;
import dk.in2isoft.onlineobjects.test.EssentialTests;

@Category(EssentialTests.class)
public class TestPasswordEncryptionService extends AbstractSpringTestCase {
	
	private static Logger log = LogManager.getLogger(TestPasswordEncryptionService.class);
	
	@Autowired
	private PasswordEncryptionService passwordEncryptionService;

	@Test
	public void testEncryption() throws Exception {
		String salt = passwordEncryptionService.generateSalt();
		String password = "$ecr3tC0de";
		String encryptedPassword = passwordEncryptionService.getEncryptedPassword(password, salt);
		assertTrue(passwordEncryptionService.authenticate(password, encryptedPassword, salt));
		
		log.info("Salt: "+salt);
		log.info("Encrypted: "+encryptedPassword);
	}
	
	public void setPasswordEncryptionService(PasswordEncryptionService passwordEncryptionService) {
		this.passwordEncryptionService = passwordEncryptionService;
	}
}