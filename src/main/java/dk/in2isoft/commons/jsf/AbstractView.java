package dk.in2isoft.commons.jsf;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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

	protected Request getRequest() {
		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
		return Request.get((HttpServletRequest) context.getRequest(),(HttpServletResponse) context.getResponse());
	}
}