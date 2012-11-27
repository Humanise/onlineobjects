package dk.in2isoft.onlineobjects.ui.jsf;

import java.io.IOException;

import javax.faces.component.FacesComponent;
import javax.faces.context.FacesContext;

import org.apache.commons.lang.StringUtils;

import dk.in2isoft.commons.jsf.AbstractComponent;
import dk.in2isoft.commons.jsf.ClassBuilder;
import dk.in2isoft.commons.jsf.ComponentUtil;
import dk.in2isoft.commons.jsf.TagWriter;
import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.services.ConfigurationService;
import dk.in2isoft.onlineobjects.ui.Request;

@FacesComponent(value = LinkComponent.FAMILY)
public class LinkComponent extends AbstractComponent {

	public static final String FAMILY = "onlineobjects.link";
	
	private String variant;
	private String title;
	private String href;
	private String onclick;
	private boolean core;
	private String styleClass;
	private boolean plain;
	private String name;
	private String app;
	
	public LinkComponent() {
		super(FAMILY);
	}
	
	@Override
	public void restoreState(Object[] state) {
		variant = (String) state[0];
		core = (Boolean) state[1];
		href = (String) state[2];
		styleClass = (String) state[3];
		onclick = (String) state[4];
		title = (String) state[5];
		plain = (Boolean) state[6];
		name = (String) state[7];
		app = (String) state[8];
	}

	@Override
	public Object[] saveState() {
		return new Object[] { variant, core, href, styleClass, onclick, title, plain, name, app };
	}

	@Override
	public String getFamily() {
		return FAMILY;
	}
	
	@Override
	protected void encodeBegin(FacesContext context, TagWriter writer) throws IOException {
		String styleClass = getStyleClass(context);
		String onclick = getOnclick(context);
		String title = getTitle(context);
		if (plain) {
			writer.startA();
		} else {
			writer.startA(ClassBuilder.with("oo_link").add("oo_link",variant).add(styleClass));
		}
		String id = getId();
		if (StringUtils.isNotBlank(id)) {
			writer.withId(id);
		} else if (Strings.isNotBlank(name)) {
			writer.withId(getClientId());
		}
		if (StringUtils.isNotBlank(onclick)) {
			writer.withAttribute("onclick", onclick);
		}
		if (StringUtils.isNotBlank(title)) {
			writer.withAttribute("title", title);
		}
		String href = getHref(context);
		if (href!=null) {
			writer.withAttribute("href", buildUrl(href, app, core));
		} else if (StringUtils.isNotBlank(id)) {
			writer.withAttribute("href","javascript: void(0);");
		}
		if (!plain) {
			writer.startSpan();
		}
	}
	
	public static String buildUrl(String href,String app,boolean core) {
		if (href.startsWith("http") || href.startsWith("#") || href.startsWith("javascript:")) {
			return href;
		} else {
			StringBuilder url = new StringBuilder();
			Request request = ComponentUtil.getRequest();
			if (app!=null) {
				ConfigurationService configurationService = ComponentUtil.getService(ConfigurationService.class, FacesContext.getCurrentInstance());
				String context = configurationService.getApplicationContext(app,request);
				if (context!=null) {
					url.append(context);
				} else {
					url.append(request.getBaseContext()).append("/app/").append(app);
				}
			} else {
				url.append(core ? request.getBaseContext() : request.getLocalContext());
			}
			url.append(href);
			return url.toString();
		}
	}
	
	@Override
	protected void encodeEnd(FacesContext context, TagWriter out) throws IOException {
		if (!plain) {
			out.endSpan();
		}
		out.endA();
		if (Strings.isNotBlank(name)) {
			out.startScript();
			out.startNewObject("oo.Link").property("element", getClientId()).comma().property("name", name).endNewObject();
			out.endScript();
		}
	}

	public void setVariant(String variant) {
		this.variant = variant;
	}

	public String getVariant() {
		return variant;
	}

	public void setCore(boolean core) {
		this.core = core;
	}

	public boolean isCore() {
		return core;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public String getHref() {
		return href;
	}
	
	public String getHref(FacesContext context) {
		return getExpression("href", href, context);
	}

	public void setStyleClass(String styleClass) {
		this.styleClass = styleClass;
	}

	public String getStyleClass() {
		return styleClass;
	}

	public String getStyleClass(FacesContext context) {
		return getExpression("styleClass",styleClass, context);
	}
	
	public void setOnclick(String onclick) {
		this.onclick = onclick;
	}
	
	public String getOnclick() {
		return onclick;
	}

	public String getOnclick(FacesContext context) {
		return getExpression("onclick", onclick, context);
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getTitle() {
		return title;
	}

	public String getTitle(FacesContext context) {
		return getExpression("title", title, context);
	}

	public boolean isPlain() {
		return plain;
	}

	public void setPlain(boolean plain) {
		this.plain = plain;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public void setApp(String app) {
		this.app = app;
	}

	public String getApp() {
		return app;
	}
}