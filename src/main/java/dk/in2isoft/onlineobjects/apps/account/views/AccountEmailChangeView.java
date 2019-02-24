package dk.in2isoft.onlineobjects.apps.account.views;

import dk.in2isoft.commons.jsf.AbstractView;
import dk.in2isoft.onlineobjects.model.Person;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.modules.user.MemberService;
import dk.in2isoft.onlineobjects.ui.Request;

public class AccountEmailChangeView extends AbstractView {

	private MemberService memberService;

	private String name;
	
	private boolean found;
	
	public void before(Request request) throws Exception {
		
		String key = request.getString("key");
		User user = memberService.performEmailChangeByKey(key, request);
		Person person = memberService.getUsersPerson(user, request.as(user));
		if (person != null) {
			name = person.getFullName();
		}
		found = true;
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
