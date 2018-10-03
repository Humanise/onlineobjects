package dk.in2isoft.onlineobjects.ui.jsf;

import java.io.IOException;
import java.util.Locale;

import javax.faces.component.FacesComponent;
import javax.faces.context.FacesContext;

import dk.in2isoft.commons.jsf.AbstractComponent;
import dk.in2isoft.commons.jsf.TagWriter;

@FacesComponent(value = HtmlComponent.FAMILY)
public class HtmlComponent extends AbstractComponent {

	public static final String FAMILY = "onlineobjects.html";
	
	public HtmlComponent() {
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
		String language = "en";
		Locale locale = context.getExternalContext().getRequestLocale();
		if (locale!=null && locale.getLanguage()!=null) {
			language = locale.getLanguage();
		}
		out.startElement("html").withAttribute("xmlns", "http://www.w3.org/1999/xhtml").withAttribute("lang", language);
	}

	@Override
	protected void encodeEnd(FacesContext context, TagWriter out) throws IOException {
		out.endElement("html");
	}

}
