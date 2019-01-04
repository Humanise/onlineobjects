package dk.in2isoft.onlineobjects.apps.account.views;

import java.util.Date;
import java.util.List;

import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Privileged;
import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.model.Property;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.modules.user.Agreement;
import dk.in2isoft.onlineobjects.modules.user.MemberService;
import dk.in2isoft.onlineobjects.ui.AbstractManagedBean;
import dk.in2isoft.onlineobjects.ui.Request;

public class AccountAgreementsView extends AbstractManagedBean {

	private MemberService memberService;
	private ModelService modelService;
	private SecurityService securityService;
	
	private boolean accepted;
	private List<Agreement> agreements;
	private Date acceptanceTime;
	private boolean publicUser;
	private String language;
	
	public void before(Request request) throws Exception {
		Privileged privileged = request.getSession();
		User user = modelService.getRequired(User.class, privileged.getIdentity(), privileged);
		accepted = memberService.hasAcceptedTerms(user, user);
		agreements = memberService.getAgreements(user, request.getLocale());
		acceptanceTime = user.getPropertyDateValue(Property.KEY_TERMS_ACCEPTANCE_TIME);
		publicUser = securityService.isPublicUser(privileged);
		language = request.getLanguage();
	}
		
	public boolean isAccepted() {
		return accepted;
	}
	
	public List<Agreement> getAgreements() {
		return agreements;
	}
	
	public Date getAcceptanceTime() {
		return acceptanceTime;
	}
	
	public boolean isPublicUser() {
		return publicUser;
	}
	
	public String getLanguage() {
		return language;
	}
		
	// Wiring...
	
	public void setMemberService(MemberService memberService) {
		this.memberService = memberService;
	}
	
	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}
	
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
}
