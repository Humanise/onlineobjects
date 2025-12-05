package dk.in2isoft.onlineobjects.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dk.in2isoft.onlineobjects.core.exceptions.ExplodingClusterFuckException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.User;

public class UserConsistencyChecker implements ConsistencyChecker {

	private static Logger log = LogManager.getLogger(UserConsistencyChecker.class);

	private ModelService modelService;
	private PasswordEncryptionService passwordEncryptionService;
	private SecurityService securityService;

	@Override
	public void check() throws ModelException, SecurityException, ExplodingClusterFuckException {
		Operation operation = modelService.newOperation();
		try {
			ensureCoreUsers(operation);
			modelService.execute(operation);
		} catch (Exception e) {
			throw e;
		}
		Operator operator = modelService.newAdminOperator();
		securityService.makePublicVisible(securityService.getPublicUser(), operator);
		operator.commit();
	}

	private void ensureCoreUsers(Operation operation) throws SecurityException, ExplodingClusterFuckException {
		User publicUser = modelService.getUser(SecurityService.PUBLIC_USERNAME, operation);
		if (publicUser == null) {
			log.warn("No public user present!");
			User user = new User();
			user.setUsername(SecurityService.PUBLIC_USERNAME);
			user.setName("Public user");
			modelService.createCoreUser(user, operation);
			log.info("Public user created!");
		}
		User adminUser = modelService.getUser(SecurityService.ADMIN_USERNAME, operation);
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
			modelService.createCoreUser(user, operation);
			log.info("Administrator created!");
		}
	}

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}

	public void setPasswordEncryptionService(PasswordEncryptionService passwordEncryptionService) {
		this.passwordEncryptionService = passwordEncryptionService;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
}
