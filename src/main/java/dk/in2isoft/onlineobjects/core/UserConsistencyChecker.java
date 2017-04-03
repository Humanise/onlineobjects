package dk.in2isoft.onlineobjects.core;

import org.apache.log4j.Logger;

import dk.in2isoft.onlineobjects.core.exceptions.ExplodingClusterFuckException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.User;

public class UserConsistencyChecker implements ConsistencyChecker {
	
	private static Logger log = Logger.getLogger(UserConsistencyChecker.class);

	private ModelService modelService;
	private PasswordEncryptionService passwordEncryptionService;

	@Override
	public void check() throws ModelException, SecurityException, ExplodingClusterFuckException {
		User publicUser = modelService.getUser(SecurityService.PUBLIC_USERNAME);
		if (publicUser == null) {
			log.warn("No public user present!");
			User user = new User();
			user.setUsername(SecurityService.PUBLIC_USERNAME);
			user.setName("Public user");
			modelService.createCoreUser(user);
			modelService.commit();
			log.info("Public user created!");
		}
		User adminUser = modelService.getUser(SecurityService.ADMIN_USERNAME);
		if (adminUser == null) {
			log.warn("No admin user present!");
			User user = new User();
			user.setUsername(SecurityService.ADMIN_USERNAME);
			user.setName("Administrator");
			String password = "changeme";
			String salt = passwordEncryptionService.generateSalt();
			String encryptedPassword = passwordEncryptionService.getEncryptedPassword(password, salt);
			user.setPassword(encryptedPassword);
			user.setSalt(salt);
			modelService.createCoreUser(user);
			modelService.commit();
			log.info("Administrator created!");
		}

	}

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}
	
	public void setPasswordEncryptionService(PasswordEncryptionService passwordEncryptionService) {
		this.passwordEncryptionService = passwordEncryptionService;
	}
}
