package dk.in2isoft.commons.jsf;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;

import dk.in2isoft.onlineobjects.ui.Request;

public abstract class AbstractView implements InitializingBean {

	public AbstractView() {
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
}