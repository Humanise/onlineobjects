package dk.in2isoft.onlineobjects.ui.jsf;

import java.io.IOException;

import javax.faces.component.FacesComponent;
import javax.faces.context.FacesContext;

import org.apache.commons.lang.StringUtils;

import dk.in2isoft.commons.jsf.AbstractComponent;
import dk.in2isoft.commons.jsf.Components;
import dk.in2isoft.commons.jsf.TagWriter;
import dk.in2isoft.onlineobjects.services.ConfigurationService;

@FacesComponent(value=AnalyticsComponent.FAMILY)
public class AnalyticsComponent extends AbstractComponent {


	public static final String FAMILY = "onlineobjects.analytics";

	public AnalyticsComponent() {
		super(FAMILY);
	}

	@Override
	public void restoreState(Object[] state) {
	}

	@Override
	public Object[] saveState() {
		return new Object[] {};
	}

	@Override
	protected void encodeBegin(FacesContext context, TagWriter out) throws IOException {
		ConfigurationService configurationService = Components.getBean(ConfigurationService.class);
		String code = configurationService.getAnalyticsCode();
		if (StringUtils.isNotBlank(code)) {
			String url = "https://www.googletagmanager.com/gtag/js?id=" + code;
			out.startScript().withAttribute("async", "async").withSrc(url).endScript();
			out.startScript();
			out.write("window.dataLayer = window.dataLayer || [];\n"
					+ "  function gtag(){dataLayer.push(arguments);}\n"
					+ "  gtag('js', new Date());\n"
					+ "  gtag('storage', 'none');\n"
					+ "  gtag('config', '" + code + "');");
			out.endScript();
		}
	}
}
