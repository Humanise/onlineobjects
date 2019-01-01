package dk.in2isoft.onlineobjects.service.authentication.views;

import dk.in2isoft.commons.jsf.AbstractView;
import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.UserSession;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.ui.Request;
import dk.in2isoft.onlineobjects.util.Messages;

public class AuthenticationLoginView extends AbstractView {
	
	private static final Messages messages = new Messages(AuthenticationLoginView.class);
	private ModelService modelService;
	private String currentUserName;
	private String redirect;
	private String message;
	
	@Override
	protected void before(Request request) throws Exception {
		super.before(request);
		redirect = request.getString("redirect");
		UserSession session = request.getSession();
		User user = modelService.get(User.class, session.getIdentity(), session);
		if (user != null) {
			currentUserName = user.getUsername();
		}
		String action = request.getString("action");
		if (Strings.isNotBlank(action)) {
			message = messages.get("action_"+action, request.getLocale());
		}
	}
	
	public String getCurrentUserName() {
		return currentUserName;
	}
	
	public String getRedirect() {
		return redirect;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}
}
