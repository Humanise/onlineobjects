package dk.in2isoft.onlineobjects.test.traffic;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.modules.user.InvitationService;
import dk.in2isoft.onlineobjects.services.EmailService;
import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;
import dk.in2isoft.onlineobjects.test.EssentialTests;

@Category(EssentialTests.class)
public class TestEmailService extends AbstractSpringTestCase {

	@Autowired
	EmailService emailService;

	@Test
	public void testVelocity() throws EndUserException {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("invited_name", "Jonas Munk");
		model.put("inviter_name", "John Andersen");
		model.put("inviter_url", "http://www.google.com/");
		model.put("invite_url", "http://www.duckduckgo.com/");
		model.put("url", "http://www.in2isoft.dk/");
		model.put("base_url", configurationService.getBaseUrl());
		String html = emailService.applyTemplate(InvitationService.INVITATION_TEMPLATE, model);
		Assert.assertTrue(html.contains("Jonas Munk"));

		//String mail = getProperty("mail.receiver.address");
		//String name = getProperty("mail.receiver.name");
		//emailService.sendHtmlMessage("Test HTML", html, mail, name);
	}
}