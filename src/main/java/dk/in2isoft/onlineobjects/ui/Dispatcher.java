package dk.in2isoft.onlineobjects.ui;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import dk.in2isoft.onlineobjects.services.DispatchingService;

public class Dispatcher implements Filter {

	FilterConfig filterConfig;
	private DispatchingService dispatchingService;

	public Dispatcher() {

	}

	public void destroy() {

	}

	public void init(FilterConfig filterConfig) {

		this.filterConfig = filterConfig;
		WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(filterConfig.getServletContext());
		dispatchingService = context.getBeansOfType(DispatchingService.class).values().iterator().next();
	}

	public void doFilter(ServletRequest sRequest, ServletResponse sResponse, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) sRequest;
		HttpServletResponse response = (HttpServletResponse) sResponse;
		dispatchingService.doFilter(request, response, chain);
	}
}
