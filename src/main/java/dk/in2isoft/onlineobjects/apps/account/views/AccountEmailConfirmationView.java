package dk.in2isoft.onlineobjects.apps.account.views;

import dk.in2isoft.commons.jsf.AbstractView;
import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.core.Pair;
import dk.in2isoft.onlineobjects.core.exceptions.ContentNotFoundException;
import dk.in2isoft.onlineobjects.core.exceptions.IllegalRequestException;
import dk.in2isoft.onlineobjects.model.EmailAddress;
import dk.in2isoft.onlineobjects.modules.user.MemberService;
import dk.in2isoft.onlineobjects.ui.Request;

public class AccountEmailConfirmationView extends AbstractView {

	private MemberService memberService;

	private String name;

	private String key;
	private String email;
	
	private boolean found;
	
	public void before(Request request) throws Exception {
		
		key = request.getString("key");
		email = request.getString("email");
		if (Strings.isBlank(key) || Strings.isBlank(email)) {
			throw new IllegalRequestException("Something is missing");
		}
		try {
			Pair<EmailAddress, String> mailToName = memberService.findEmailByConfirmationKey(key, request);
			memberService.markConfirmed(mailToName.getKey(), request);
			name = mailToName.getValue();
			found = true;
		} catch (ContentNotFoundException e) {
			// TODO Maybe log this
		}
	}
	
	public boolean isFound() {
		return found;
	}
	
	public String getName() {
		return name;
	}
	
	// Wiring...
	
	public void setMemberService(MemberService memberService) {
		this.memberService = memberService;
	}
}
