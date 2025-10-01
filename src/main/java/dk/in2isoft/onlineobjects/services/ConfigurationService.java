package dk.in2isoft.onlineobjects.services;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import dk.in2isoft.commons.lang.Files;
import dk.in2isoft.onlineobjects.apps.ApplicationController;
import dk.in2isoft.onlineobjects.core.exceptions.ConfigurationException;
import dk.in2isoft.onlineobjects.ui.Request;

public class ConfigurationService implements InitializingBean {

	private static Logger log = LogManager.getLogger(ConfigurationService.class);

	private String baseUrl;
	private String basePath;
	private String storagePath;
	private boolean developmentMode;
	private String imageMagickPath;
	private String uglifyPath;
	private String graphvizPath;
	private String developmentUser;
	private String analyticsCode;
	private String rootDomain;
	private Integer port;
	private boolean startScheduling;
	private boolean simulateSlowRequest;
	private boolean simulateSporadicServerError;
	private boolean testMode;
	private String appleAppSiteAssociation;
	private String monitoringMails;
	private boolean migrateDatabaseSchema;
	private boolean https;
	private boolean intelligenceEnabled;
	private boolean solrEnabled;
	private String languageModel;
	private String ollamaUrl;

	private File tempDir;

	private File storageDir;

	private File indexDir;

	private Multimap<String, Locale> appLocales = HashMultimap.create();
	private Map<String, String> appMountPoints = Maps.newHashMap();

	private LifeCycleService lifeCycleService;

	private boolean optimizeResources;

	private boolean simulateHttps;
	private boolean disableCache;

	private String anthropicApiKey;


	@Override
	public void afterPropertiesSet() throws Exception {
		if (!new File(basePath).isDirectory()) {
			throw new ConfigurationException("The base path is not a dir: " + basePath);
		}
		storageDir = new File(storagePath);
		if (!storageDir.canWrite()) {
			throw new ConfigurationException("Unable to write to storage directory on path: "+storageDir);
		}
		tempDir = new File(storageDir,"temporary");
		if (!tempDir.isDirectory()) {
			if (!tempDir.mkdir()) {
				throw new ConfigurationException("Could not create temporary directory");
			}
			log.info("Created temporary directory");
		} else if (!tempDir.canWrite()) {
			throw new ConfigurationException("Can not write to the temporary directory");
		}

		indexDir = new File(storageDir,"index");
		if (!indexDir.isDirectory()) {
			if (!indexDir.mkdir()) {
				throw new ConfigurationException("Could not create index directory");
			}
			log.info("Created index directory");
		} else if (!indexDir.canWrite()) {
			throw new ConfigurationException("Can not write to the index directory");
		}
	}

	@Autowired
	public void setApplicationControllers(Collection<? extends ApplicationController> controllers) {
		for (ApplicationController controller : controllers) {
			if (controller.getLocales()!=null) {
				appLocales.putAll(controller.getName(), controller.getLocales());
			}
			appMountPoints.put(controller.getName(), controller.getMountPoint());
		}
	}

	public File getTempDir() {
		return tempDir;
	}

	public File getFile(String... path) {
		StringBuilder name = new StringBuilder();
		name.append(basePath);
		for (int i = 0; i < path.length; i++) {
			name.append(File.separatorChar);
			name.append(path[i]);
		}
		File file = new File(name.toString());
		if (isDevelopmentMode()) {
			// If in dev mode, check that it exists
			if (!Files.checkSensitivity(file)) {
				throw new IllegalStateException("Not exact case: " + file.getAbsolutePath());
			}
		}
		return file;
	}

	/**
	 * TODO: Move to other service
	 */
	public File findExistingFile(String[] path) {
		StringBuilder filePath = new StringBuilder();
		filePath.append(basePath);
		for (int i = 0; i < path.length; i++) {
			filePath.append(File.separator);
			filePath.append(path[i]);
		}
		File file = new File(filePath.toString());

		if (file.exists() && !file.isDirectory()) {
			return file;
		}

		return null;
	}

	public void setDisableCache(boolean disableCache) {
		this.disableCache = disableCache;
	}

	public boolean isDisableCache() {
		return disableCache;
	}

	public String getDeploymentId() {
		return String.valueOf(lifeCycleService.getStartTime().getTime());
	}

	public Date getDeploymentTime() {
		return lifeCycleService.getStartTime();
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setDevelopmentMode(boolean developmentMode) {
		this.developmentMode = developmentMode;
	}

	public boolean isDevelopmentMode() {
		return developmentMode;
	}

	public boolean isOptimizeResources() {
		return optimizeResources;
	}

	public void setOptimizeResources(boolean optimizeResources) {
		this.optimizeResources = optimizeResources;
	}

	public void setStoragePath(String storagePath) {
		this.storagePath = storagePath;
	}

	public File getStorageDir() {
		return storageDir;
	}

	public File getIndexDir() {
		return indexDir;
	}

	public String getStoragePath() {
		return storagePath;
	}

	public void setImageMagickPath(String imageMagickPath) {
		this.imageMagickPath = imageMagickPath;
	}

	public String getImageMagickPath() {
		return imageMagickPath;
	}

	public void setGraphvizPath(String graphvizPath) {
		this.graphvizPath = graphvizPath;
	}

	public String getGraphvizPath() {
		return graphvizPath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	public String getBasePath() {
		return basePath;
	}

	public String getDevelopmentUser() {
		return developmentUser;
	}

	public void setDevelopmentUser(String developmentUser) {
		this.developmentUser = developmentUser;
	}

	public String getAnalyticsCode() {
		return analyticsCode;
	}

	public void setAnalyticsCode(String analyticsCode) {
		this.analyticsCode = analyticsCode;
	}

	public Collection<Locale> getApplicationLocales(String app) {
		return appLocales.get(app);
	}

	public String getApplicationContext(String app) {
		StringBuilder url = new StringBuilder();
		if (https) {
			url.append("https://");
		} else {
			url.append("http://");
		}
		url.append(appMountPoints.get(app)).append(".").append(rootDomain);
		return url.toString();
	}

	public String getApplicationContext(String app, String path, Request request) {
		Locale locale = request.getLocale();
		if (StringUtils.isBlank(rootDomain)) {
			return request.getBaseContext() + "/app/words/" + locale.getLanguage() + "/";
		}
		HttpServletRequest servletRequest = request.getRequest();
		StringBuilder url = new StringBuilder();
		String scheme = servletRequest.getScheme();
		int port = servletRequest.getServerPort();
		if (simulateHttps) {
			scheme = "https";
			port = 443;
		} else if (this.port != null) {
			port = this.port;
		}
		url.append(scheme).append("://").append(appMountPoints.get(app)).append(".").append(rootDomain);
		if (port != 80 && port != 443) {
			url.append(":").append(port);
		}
		url.append(request.getBaseContext());
		if (appLocales.containsEntry(app, locale)) {
			url.append("/").append(locale.getLanguage());
		}
		if (path!=null) {
			if (!path.startsWith("/")) {
				url.append("/");
			}
			url.append(path);
		} else {
			url.append("/");
		}
		String full = url.toString();
		return full;
	}

	public String getRootDomain() {
		return rootDomain;
	}

	public void setRootDomain(String rootDomain) {
		this.rootDomain = rootDomain;
	}

	public void setLifeCycleService(LifeCycleService lifeCycleService) {
		this.lifeCycleService = lifeCycleService;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public boolean isStartScheduling() {
		return startScheduling;
	}

	public void setStartScheduling(boolean startScheduling) {
		this.startScheduling = startScheduling;
	}

	public boolean isSimulateSlowRequest() {
		return simulateSlowRequest;
	}

	public void setSimulateSlowRequest(boolean simulateSlowRequest) {
		this.simulateSlowRequest = simulateSlowRequest;
	}

	public void setSimulateHttps(boolean simulateHttps) {
		this.simulateHttps = simulateHttps;
	}

	public boolean isTestMode() {
		return testMode;
	}

	public void setTestMode(boolean testMode) {
		this.testMode = testMode;
	}

	public String getAppleAppSiteAssociation() {
		return appleAppSiteAssociation;
	}

	public void setAppleAppSiteAssociation(String appleAppSiteAssociation) {
		this.appleAppSiteAssociation = appleAppSiteAssociation;
	}

	public String getMonitoringMails() {
		return monitoringMails;
	}

	public void setMonitoringMails(String monitoringMails) {
		this.monitoringMails = monitoringMails;
	}

	public boolean isSimulateSporadicServerError() {
		return simulateSporadicServerError;
	}

	public void setSimulateSporadicServerError(boolean simulateSporadicServerError) {
		this.simulateSporadicServerError = simulateSporadicServerError;
	}

	public String getUglifyPath() {
		return uglifyPath;
	}

	public void setUglifyPath(String uglifyPath) {
		this.uglifyPath = uglifyPath;
	}

	public boolean isMigrateDatabaseSchema() {
		return migrateDatabaseSchema;
	}

	public void setMigrateDatabaseSchema(boolean migrateDatabaseSchema) {
		this.migrateDatabaseSchema = migrateDatabaseSchema;
	}

	public boolean isHttps() {
		return https;
	}

	public void setHttps(boolean https) {
		this.https = https;
	}

	public boolean isIntelligenceEnabled() {
		return intelligenceEnabled;
	}

	public void setIntelligenceEnabled(boolean intelligenceEnabled) {
		this.intelligenceEnabled = intelligenceEnabled;
	}

	public boolean isSolrEnabled() {
		return solrEnabled;
	}

	public void setSolrEnabled(boolean solrEnabled) {
		this.solrEnabled = solrEnabled;
	}

	public String getLanguageModel() {
		return languageModel;
	}

	public void setLanguageModel(String languageModel) {
		this.languageModel = languageModel;
	}

	public String getAnthropicApiKey() {
		return this.anthropicApiKey;
	}

	public void setAnthropicApiKey(String anthropicApiKey) {
		this.anthropicApiKey = anthropicApiKey;
	}

	public String getOllamaUrl() {
		return ollamaUrl;
	}

	public void setOllamaUrl(String ollamaUrl) {
		this.ollamaUrl = ollamaUrl;
	}
}
