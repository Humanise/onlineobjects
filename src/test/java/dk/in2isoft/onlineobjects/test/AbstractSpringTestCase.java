package dk.in2isoft.onlineobjects.test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import dk.in2isoft.commons.lang.Files;
import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Privileged;
import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.services.ConfigurationService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:applicationContext.test.xml")
public abstract class AbstractSpringTestCase extends AbstractJUnit4SpringContextTests {
	
	@Autowired
	protected ConfigurationService configurationService;
	
	@Autowired
	private ApplicationContext context;
	
	@Autowired
	protected ModelService modelService;

	@Autowired
	protected SecurityService securityService;
	
	private static boolean cleaned = false;
	
	@BeforeClass
	public static void before() throws IOException {
		if (!cleaned) {
			Properties props = PropertiesLoaderUtils.loadAllProperties("configuration.test.properties");
			String path = props.getProperty("storagePath");
			
			File storage = new File(path);
			
			File[] dirs = storage.listFiles();
			for (File file : dirs) {
				if (!file.isDirectory()) continue;
				try {
					System.out.println("Removing dir: " + file);
					FileUtils.deleteDirectory(file);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			cleaned = true;
		}
	}
	
	@After
	public void after() {
		long x = System.currentTimeMillis() + 5000;
		while (System.currentTimeMillis() < x) {
			if (modelService.getActiveOperationCount() > 0) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {}
			} else {
				break;
			}
		}
		assertEquals(0, modelService.getActiveOperationCount());
	}

	protected File getTestFile(String name) throws IOException {
		File file = context.getResource(name).getFile();
		if (!file.exists()) {
			throw new IllegalStateException("The test file does not exist: "+name);
		}
		return file;
	}

	protected void writeTestFile(String data, String name) throws IOException {
		File file = context.getResource(name).getFile();
		Files.overwriteTextFile(data, file);
	}

	protected String getTestFileAsString(String name) throws IOException {
		File file = getTestFile(name);
		try (FileReader reader = new FileReader(file)) {
			return IOUtils.toString(reader);
		}
	}

	protected User getPublicUser() {
		return securityService.getPublicUser();
	}

	protected Privileged getAdminUser() {
		return securityService.getAdminPrivileged();
	}
	
	protected User getNewTestUser() {
		User user = new User();
		user.setUsername(getUniqueTestUserName());
		return user;
	}

	protected String getUniqueTestUserName() {
		return "testuser" + System.currentTimeMillis() + Math.round(Math.random() * 9999999);
	}

	public void setContext(ApplicationContext context) {
		this.context = context;
	}

	public ApplicationContext getContext() {
		return context;
	}

	protected void print(String string, Object object) {
		System.out.println(string+": "+object);
	}

	protected File getResourcesDir() {
		return new File(getProperty("test.resources.dir"));
	}

	protected String getProperty(String name) {
		Resource resource = context.getResource("test.properties");
		Properties p = new Properties();
		try {
			p.load(resource.getInputStream());
			return p.getProperty(name);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	protected File getOutputDir() {

		String outputDir = getProperty("output.dir");
		if (Strings.isBlank(outputDir)) {
			throw new IllegalStateException("The output.dir property is not set");
		}
		File dir = new File(outputDir);
		if (!dir.exists()) {
			throw new IllegalStateException("The output dir does not exists: " + outputDir);
		}
		if (!dir.isDirectory()) {
			throw new IllegalStateException("The output dir is not a folder: " + outputDir);
		}
		if (!dir.canWrite()) {
			throw new IllegalStateException("The output dir can not be written: " + outputDir);
		}
		return dir;
	}

	
	protected void assertFails(FailableRunnable runnable) {
		boolean caught = false;
		try {
			runnable.run();
		} catch (EndUserException e) {
			// Catch exception when trying to delete
			caught = true;
		}
		assertTrue(caught);
		
	}
	
	@FunctionalInterface
	protected interface FailableRunnable {
	    public abstract void run() throws EndUserException;
	}
	
	// Wiring...
	
	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}

	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
}
