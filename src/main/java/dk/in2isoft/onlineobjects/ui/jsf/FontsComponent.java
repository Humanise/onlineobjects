package dk.in2isoft.onlineobjects.ui.jsf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.faces.component.FacesComponent;
import javax.faces.context.FacesContext;

import dk.in2isoft.commons.jsf.AbstractComponent;
import dk.in2isoft.commons.jsf.TagWriter;
import dk.in2isoft.commons.lang.Strings;

@FacesComponent(value = FontsComponent.FAMILY)
public class FontsComponent extends AbstractComponent {

	public static final String FAMILY = "onlineobjects.fonts";
	
	private String additional;
	
	public FontsComponent() {
		super(FAMILY);
	}
	
	@Override
	public Object[] saveState() {
		return new Object[] {};
	}
	
	@Override
	public void restoreState(Object[] state) {
	}
	
	@Override
	protected void encodeBegin(FacesContext context, TagWriter out) throws IOException {
		List<String> googleFonts = new ArrayList<String>();
		if (Strings.isNotBlank(additional)) {
			googleFonts.add(additional);
		}
		String testFont = getRequest().getString("_font");
		if (Strings.isNotBlank(testFont)) {
			if ("Inter".equals(testFont)) {
				out.startElement("link").withHref("https://rsms.me/inter/inter.css").rel("stylesheet").type("text/css").endElement("link");
			} else {
				googleFonts.add(Strings.encodeURL(testFont) + ":300,400,500,600,700");
			}
			out.startElement("style").text(".oo_body {font-family: '"+testFont+"', Arial !important;}").endElement("style");
		}
		out.startElement("link").withHref("https://use.typekit.net/dqs7hkt.css").rel("stylesheet").type("text/css").endElement("link");
		if (!googleFonts.isEmpty()) {
			out.startElement("link").withHref("https://fonts.googleapis.com/css?family="+googleFonts.stream().collect(Collectors.joining("|"))+"&display=swap").rel("stylesheet").type("text/css").endElement("link");
		}
	}

	public String getAdditional() {
		return additional;
	}

	public void setAdditional(String additional) {
		this.additional = additional;
	}
}
