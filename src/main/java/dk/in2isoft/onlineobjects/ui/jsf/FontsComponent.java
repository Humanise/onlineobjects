package dk.in2isoft.onlineobjects.ui.jsf;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.faces.component.FacesComponent;
import jakarta.faces.context.FacesContext;

import com.yahoo.platform.yui.compressor.CssCompressor;

import dk.in2isoft.commons.jsf.AbstractComponent;
import dk.in2isoft.commons.jsf.Components;
import dk.in2isoft.commons.jsf.TagWriter;
import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.modules.caching.CacheService;
import dk.in2isoft.onlineobjects.modules.networking.NetworkService;

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
		List<String> urls = new ArrayList<>();
		List<String> googleFonts = new ArrayList<>();
		googleFonts.add("Inter:100,200,300,400,500,600,700");
		googleFonts.add("Hind+Siliguri:400,500,600,700");
		//urls.add("https://use.typekit.net/dqs7hkt.css");
		if (Strings.isNotBlank(additional)) {
			String[] parts = additional.split(";");
			for (String string : parts) {
				googleFonts.add(string);
			}
		}
		String testFont = getRequest().getString("_font");
		if (Strings.isNotBlank(testFont)) {
			{
				googleFonts.add(Strings.encodeURL(testFont) + ":300,400,500,600,700");
			}
			out.startElement("style").text(".oo_body {font-family: '"+testFont+"', Arial !important;}").endElement("style");
		}
		if (!googleFonts.isEmpty()) {
			urls.add("https://fonts.googleapis.com/css?family="+googleFonts.stream().collect(Collectors.joining(Strings.encodeURL("|")))+"&display=swap");
		}
		out.startElement("style");
		for (String url : urls) {
			String css = getFontCSS(url);
			out.text(css);
		}
		out.endElement("style");
	}

	private String getFontCSS(String url) {
		CacheService service = Components.getService(CacheService.class, getFacesContext());
		NetworkService networkService = getBean(NetworkService.class);
		return service.getCachedDocument("font-" + url, () -> {
			String css = networkService.getStringSilently(url);
			return compress(css.replaceAll("@import.*", "").replaceAll("font-display:auto", "font-display:swap"));
		});
	}

	private String compress(String css) {
		try {
			CssCompressor compressor = new CssCompressor(new StringReader(css));
			StringWriter writer = new StringWriter();
			compressor.compress(writer, -1);
			return writer.toString();
		} catch (IOException e) {
			return css;
		}
	}

	public String getAdditional() {
		return additional;
	}

	public void setAdditional(String additional) {
		this.additional = additional;
	}
}
