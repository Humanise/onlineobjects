package dk.in2isoft.onlineobjects.ui.jsf;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.faces.component.FacesComponent;
import javax.faces.context.FacesContext;

import com.google.common.collect.Lists;
import com.google.gson.Gson;

import dk.in2isoft.commons.jsf.AbstractComponent;
import dk.in2isoft.commons.jsf.Components;
import dk.in2isoft.commons.jsf.Dependencies;
import dk.in2isoft.commons.jsf.TagWriter;
import dk.in2isoft.in2igui.jsf.BoxComponent;
import dk.in2isoft.in2igui.jsf.ButtonComponent;
import dk.in2isoft.in2igui.jsf.CheckboxComponent;
import dk.in2isoft.in2igui.jsf.FormulaComponent;
import dk.in2isoft.in2igui.jsf.ListComponent;
import dk.in2isoft.in2igui.jsf.MessageComponent;
import dk.in2isoft.in2igui.jsf.PanelComponent;
import dk.in2isoft.in2igui.jsf.SourceComponent;
import dk.in2isoft.in2igui.jsf.TextInputComponent;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.services.ConfigurationService;
import dk.in2isoft.onlineobjects.ui.Request;
import dk.in2isoft.onlineobjects.util.Messages;

@FacesComponent(value = TopBarComponent.FAMILY)
@Dependencies(js = { "/WEB-INF/core/web/js/oo_topbar.js" }, css = { "/WEB-INF/core/web/css/oo_topbar.css" }, requires = { OnlineObjectsComponent.class, IconComponent.class }, uses = {
		PanelComponent.class, FormulaComponent.class, TextInputComponent.class, ButtonComponent.class, BoxComponent.class, ListComponent.class, SourceComponent.class, MessageComponent.class, LinkComponent.class, CheckboxComponent.class })
public class TopBarComponent extends AbstractComponent {

	public static final String FAMILY = "onlineobjects.topBar";

	private static List<String> primaryApps = Lists.newArrayList("words", "photos", "people", "knowledge");
	private static List<String> privateApps = Lists.newArrayList(); //, "desktop", "tools"
	private static Map<String,String> icons = new HashMap<String, String>();

	private static String[] textKeys = new String[] {"forgot_password","username","password","log_in","log_out","change_user","account","profile","you_are_logged_in","create_account"};;
	
	static {
		icons.put("words", "app_words");
		icons.put("people", "app_people");
		icons.put("photos", "app_photos");
		icons.put("knowledge", "app_knowledge");
		icons.put("desktop", "view_grid");
		icons.put("tools", "archive_line_selected");
	}

	public TopBarComponent() {
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
		SecurityService securityService = Components.getBean(SecurityService.class);
		// boolean developmentMode = configurationService.isDevelopmentMode();
		Request request = Request.get(context);
		Messages msg = new Messages(this);
		Locale locale = request.getLocale();
		String texts = buildTexts(locale, msg);

		
		out.startDiv("oo_topbar oo_faded").withId(getClientId()).withAttribute("data-texts", texts);
		// TODO: Get this via application controller
		if ("knowledge".equals(request.getApplication())) {
			out.withAttribute("data-login-url", "/"+request.getLanguage()+"/app");
			out.withAttribute("data-logout-url", "/"+request.getLanguage()+"/intro");
		}
		out.startA("oo_topbar_logo").withHref(configurationService.getApplicationContext("front", null, request));
		out.startEm("oo_topbar_logo_icon oo_icon_onlineobjects").endEm();
		out.startSpan("oo_topbar_logo_text").startSpan("oo_topbar_logo_part").text("Online").endSpan().text("Objects").endSpan();
		out.endA();

		out.startUl("oo_topbar_menu oo_topbar_left");
		/*
		 * out.startLi("oo_topbar_people").startA(request.isApplication("community"
		 * ) ? "oo_topbar_selected" : null); if (developmentMode) {
		 * out.withHref(request.getBaseContext()+"/"); } else {
		 * out.withHref("http://www.onlineme.dk/"); }
		 * out.write("Community").endA().endLi();
		 */

		for (String app : primaryApps) {
			boolean selected = request.isApplication(app);
			out.startLi("oo_topbar_menu_item oo_topbar_" + app + (selected ? " is-selected" : ""));
			String cls = "oo_topbar_item oo_topbar_menu_link";
			if (selected) {
				cls += " is-selected";
			}
			out.startA(cls).withAttribute("data-icon", icons.get(app));;
			out.withHref(configurationService.getApplicationContext(app, null, request));
			out.text(msg.get("app_" + app, locale)).endA().endLi();
		}

		boolean publicUser = securityService.isPublicUser(request.getSession());
		
		if (!publicUser && !privateApps.isEmpty()) {
			for (String app : privateApps) {
				boolean selected = request.isApplication(app);
				out.startLi("oo_topbar_menu_item oo_topbar_" + app + (selected ? " is-selected" : ""));
				String cls = "oo_topbar_item oo_topbar_menu_link";
				if (selected) {
					cls += " is-selected";
				}
				out.startA(cls).withAttribute("data-icon", icons.get(app));
				out.withHref(configurationService.getApplicationContext(app, null, request));
				out.text(msg.get("app_" + app, locale)).endA().endLi();
			}
		}

		out.endUl();


		out.startUl("oo_topbar_right");
		if (publicUser) {
			out.startLi("oo_topbar_right_item").startVoidA("oo_topbar_item oo_topbar_login").withAttribute("data", "login").write(msg.get("log_in", locale)).endA().endLi();
		} else {
			//InboxService inboxService = Components.getBean(InboxService.class);
			ModelService modelService = Components.getBean(ModelService.class);
			try {
				User user = modelService.getRequired(User.class, request.getSession().getIdentity(), request.getSession());
				//int count = inboxService.getCountSilently(user);
				//out.startLi("oo_topbar_right_item").startVoidA("oo_topbar_inbox").withAttribute("data", "inbox").text(count).endA().endLi();
				out.startLi("oo_topbar_right_item").startVoidA("oo_topbar_item oo_topbar_user").withAttribute("data", "user");
				out.startSpan().withClass("oo_icon oo_icon_16 oo_icon_user oo_topbar_user_icon").endSpan();
				out.write(user.getName()).endA().endLi();
			} catch (EndUserException e) {
				
			}
		}
		out.endUl();
	}

	private String buildTexts(Locale locale, Messages msg) {
		Map<String,String> texts = new HashMap<>();
		for (String key : textKeys) {
			texts.put(key, msg.get(key, locale));
		}
		return new Gson().toJson(texts);
	}

	@Override
	protected void encodeEnd(FacesContext context, TagWriter writer) throws IOException {
		writer.endDiv();
		writer.getScriptWriter().startScript().startNewObject("oo.TopBar").property("element", getClientId()).endNewObject().endScript();
	}
}
