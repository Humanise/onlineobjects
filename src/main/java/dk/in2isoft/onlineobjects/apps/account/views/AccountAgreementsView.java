package dk.in2isoft.onlineobjects.apps.account.views;

import java.util.Date;
import java.util.List;

import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.model.Property;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.modules.user.Agreement;
import dk.in2isoft.onlineobjects.modules.user.MemberService;
import dk.in2isoft.onlineobjects.ui.AbstractManagedBean;
import dk.in2isoft.onlineobjects.ui.Request;

public class AccountAgreementsView extends AbstractManagedBean {

	private MemberService memberService;
	private ModelService modelService;
	
	private boolean accepted;
	private List<Agreement> agreements;
	private Date acceptanceTime;
	
	public void before(Request request) throws Exception {
		User user = request.getSession().getUser();
		user = modelService.getRequired(User.class, user.getId(), user);
		accepted = memberService.hasAcceptedTerms(user, user);
		agreements = memberService.getAgreements(user, request.getLocale());
		acceptanceTime = user.getPropertyDateValue(Property.KEY_TERMS_ACCEPTANCE_TIME);
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
		
	// Wiring...
	
	public void setMemberService(MemberService memberService) {
		this.memberService = memberService;
	}
	
	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}
}
