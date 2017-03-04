package dk.in2isoft.onlineobjects.test.traffic;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.modules.user.InvitationService;
import dk.in2isoft.onlineobjects.services.EmailService;
import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;

public class TestEmailService extends AbstractSpringTestCase {
	
	@Autowired
	private EmailService emailService;
		
	@Test
	public void testVelocity() throws EndUserException {
		Map<String, Object> model = new HashMap<String, Object>();
        model.put("invited-name", "Jonas Munk");
        model.put("inviter-name", "John Andersen");
        model.put("inviter-url", "http://www.google.com/");
        model.put("invite-url", "http://www.duckduckgo.com/");
        model.put("url", "http://www.in2isoft.dk/");
        model.put("base-url", configurationService.getBaseUrl());
        String html = emailService.applyTemplate(InvitationService.INVITATION_TEMPLATE, model);
        Assert.assertTrue(html.contains("Jonas Munk"));
        String mail = getProperty("mail.receiver.address");
        String name = getProperty("mail.receiver.name");
		emailService.sendHtmlMessage("Test HTML",html,mail,name);
	}

	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}

	public EmailService getEmailService() {
		return emailService;
	}
}