package dk.in2isoft.onlineobjects.services;

import java.io.File;
import java.util.Map;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.ui.velocity.VelocityEngineUtils;

import dk.in2isoft.commons.lang.Files;
import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;

public class EmailService {

	private static final Logger log = LogManager.getLogger(EmailService.class); 
	private ConfigurationService configurationService;
	
	private VelocityEngine velocityEngine;
	
	private String host;
	private String username;
	private String password;
	private String defaultSenderAddress;
	private String defaultSenderName;

	public void sendMessage(String subject, String body, String address) throws EndUserException {
		sendMessage(subject, body, null, address, null);
	}

	public String applyTemplate(String path, Map<String, Object> model) {
        return VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, path, Strings.UTF8, model);
	}
	
	public void sendMessage(String subject, String textBody, String address, String name) throws EndUserException {
		sendMessage(subject, textBody, null, address, name);
	}
	
	public void sendHtmlMessage(String subject, String htmlBody, String address, String name) throws EndUserException {
		sendMessage(subject, null, htmlBody, address, name);
	}

	private void sendMessage(String subject, String textBody, String htmlBody, String address, String name) throws EndUserException {
		if (configurationService.isTestMode()) {
			File file = new File(configurationService.getTempDir(), address+".html");
			Files.overwriteTextFile(htmlBody, file);
			return;
		}
		try {
			HtmlEmail email = new HtmlEmail();
			email.setCharset("UTF-8");
			email.setHostName(host);
			email.setSSL(true);
			email.setSslSmtpPort("465");
			email.setAuthentication(username, password);
			email.addTo(address,name);
			email.setFrom(defaultSenderAddress, defaultSenderName);
			email.setSubject(subject);
			if (htmlBody!=null) {
				email.setHtmlMsg(htmlBody);
			}
			if (textBody!=null) {
				email.setMsg(textBody);
			}
			//email.setDebug(config);
			log.info("Sending email to: "+address);
			email.send();
			log.info("Sent email to: "+address);
		} catch (EmailException e) {
			log.error("Could not send email to: "+address,e);
			throw new EndUserException(e);
		}
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getHost() {
		return host;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public void setDefaultSenderAddress(String defaultSenderAddress) {
		this.defaultSenderAddress = defaultSenderAddress;
	}

	public String getDefaultSenderAddress() {
		return defaultSenderAddress;
	}

	public void setDefaultSenderName(String defaultSenderName) {
		this.defaultSenderName = defaultSenderName;
	}

	public String getDefaultSenderName() {
		return defaultSenderName;
	}

	public void setVelocityEngine(VelocityEngine velocityEngine) {
		this.velocityEngine = velocityEngine;
	}

	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}
}
