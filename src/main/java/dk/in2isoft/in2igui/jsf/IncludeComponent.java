package dk.in2isoft.in2igui.jsf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.faces.component.FacesComponent;
import jakarta.faces.context.FacesContext;

import dk.in2isoft.commons.jsf.AbstractComponent;
import dk.in2isoft.commons.jsf.TagWriter;
import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.services.ConfigurationService;
import dk.in2isoft.onlineobjects.ui.HUIService;
import dk.in2isoft.onlineobjects.ui.jsf.DependableComponent;

@FacesComponent(value = IncludeComponent.TYPE)
public class IncludeComponent extends AbstractComponent implements DependableComponent {

	public static final String TYPE = "hui.include";

	private String path;

	public IncludeComponent() {
		super(TYPE);
	}

	@Override
	public void restoreState(Object[] state) {
		path = (String) state[0];
	}

	@Override
	public Object[] saveState() {
		return new Object[] { getPath() };
	}

	private String html;

	private String getHTML() {
		if (html != null) return html;
		File file = getFile();
		HUIService hui = getBean(HUIService.class);
		try {
			html = hui.render(file, getRequest().getRequest());
		} catch (IOException e) {

		}
		return html;
	}

	private String _head;
	private String getHead() {
		if (_head == null) {
			_head = extractTag("head", getHTML());
		}
		return _head;
	}

	@Override
	public void encodeBegin(FacesContext context, TagWriter out) throws IOException {
		String body = extractTag("body", getHTML());
		body = filterScripts(body,out.getScriptWriter());
		out.write(body);
	}

	private List<String> extractCSS(String html) {
		List<String> css = new ArrayList<>();
		if (html != null) {
			Pattern p = Pattern.compile("<link[^>]+href=\"([^\"]*)\"");
			Matcher matcher = p.matcher(html);
			while (matcher.find()) {
				String href = matcher.group(1);
				if (!href.contains("hui/bin")) {
					css.add(urlToPath(href));
				}
			}
		}
		return css;
	}

	private List<String> extractJS(String html) {
		List<String> list = new ArrayList<>();
		if (html != null) {
			Pattern p = Pattern.compile("<script[^>]+src=\"([^\"]*)\"");
			Matcher matcher = p.matcher(html);
			while (matcher.find()) {
				String href = matcher.group(1);
				if (!href.contains("hui/bin")) {
					list.add(urlToPath(href));
				}
			}
		}
		return list;
	}

	private String filterScripts(String html, dk.in2isoft.commons.jsf.ScriptWriter scriptWriter) throws IOException {
		if (html == null) return html;
		int pos = 0;
		StringBuilder filtered = new StringBuilder();
		Pattern p = Pattern.compile("<script[^>]*>");
		Matcher matcher = p.matcher(html);
		while (matcher.find()) {
			filtered.append(html.substring(pos, matcher.start()));
			int end = html.indexOf("</script>", matcher.end());
			scriptWriter.startScript().write(html.substring(matcher.end(), end)).endScript();
			pos = end + "</script>".length();
		}
		filtered.append(html.substring(pos));
		return filtered.toString();
	}

	private File getFile() {
		ConfigurationService config = getBean(ConfigurationService.class);
		StringBuilder filePath = new StringBuilder();
		filePath.append(config.getBasePath());
		filePath.append(File.separator);
		filePath.append("apps");
		filePath.append(File.separator);
		filePath.append(getRequest().getApplication());
		filePath.append(File.separator).append(path);
		File file = new File(filePath.toString());
		return file;
	}

	private String extractTag(String tag, String html) {
		Pattern p = Pattern.compile("<"+tag+"[^>]*>");
		Matcher matcher = p.matcher(html);
		if (matcher.find()) {
			return html.substring(matcher.end(), html.lastIndexOf("</"+tag));
		}
		return null;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public String[] getScripts(FacesContext context) {
		List<String> base = getBean(HUIService.class).getBaseJS();
		List<String> joined = new ArrayList<>(base);
		joined.addAll(getLocalJS());
		return Strings.toArray(joined);
	}

	@Override
	public String[] getStyles(FacesContext context) {
		List<String> base = getBean(HUIService.class).getBaseCSS();
		List<String> joined = new ArrayList<>(base);
		joined.addAll(getLocalCSS());
		return Strings.toArray(joined);
	}

	private List<String> getLocalCSS() {
		String head = getHead();
		return extractCSS(head);
	}

	private List<String> getLocalJS() {
		String head = getHead();
		return extractJS(head);
	}

	private String urlToPath(String url) {
		if (url.startsWith("/core")) {
			return url;
		}
		else if (url.startsWith("/hui")) {
			return url;
		} else {
			return "/apps/" + getRequest().getApplication() + url;
		}
	}

	@Override
	public Class<?>[] getComponents(FacesContext context) {
		return null;
	}
}
