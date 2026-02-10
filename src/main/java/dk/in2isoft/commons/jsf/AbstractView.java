package dk.in2isoft.commons.jsf;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import dk.in2isoft.onlineobjects.ui.Request;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if (facesContext != null) {
			ExternalContext context = facesContext.getExternalContext();
			return Request.get((HttpServletRequest) context.getRequest(),(HttpServletResponse) context.getResponse());
		}
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		return Request.get(attributes.getRequest(),attributes.getResponse());
	}
}