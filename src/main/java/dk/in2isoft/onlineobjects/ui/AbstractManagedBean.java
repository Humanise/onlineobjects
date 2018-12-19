package dk.in2isoft.onlineobjects.ui;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;

import dk.in2isoft.onlineobjects.core.SecurityService;

public abstract class AbstractManagedBean implements InitializingBean {

	public AbstractManagedBean() {
		super();
	}

	@Override
	public final void afterPropertiesSet() throws Exception {
		before(getRequest());
	}

	protected void before(Request request) throws Exception {
		
	}
	
	private Request getRequest() {
		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
		return Request.get((HttpServletRequest) context.getRequest(),(HttpServletResponse) context.getResponse());
	}
	
	public String getBaseContext() {
		return getRequest().getBaseContext();
	}
	
	public String getBaseDomainContext() {
		return getRequest().getBaseDomainContext();
	}
	
	public String getLocalContext() {
		return getRequest().getLocalContext();
	}
	
	public boolean getIsIP() {
		return getRequest().isIP();
	}

	public String getUserName() {
		return getRequest().getSession().getUsername();
	}
	
	public boolean isPublicUser() {
		return SecurityService.PUBLIC_USERNAME.equals(getUserName());
	}
}