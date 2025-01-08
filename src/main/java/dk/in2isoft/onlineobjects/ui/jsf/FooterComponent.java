package dk.in2isoft.onlineobjects.ui.jsf;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import javax.faces.component.FacesComponent;
import javax.faces.context.FacesContext;

import dk.in2isoft.commons.jsf.AbstractComponent;
import dk.in2isoft.commons.jsf.Components;
import dk.in2isoft.commons.jsf.Dependencies;
import dk.in2isoft.commons.jsf.TagWriter;
import dk.in2isoft.onlineobjects.apps.account.AccountController;
import dk.in2isoft.onlineobjects.services.ConfigurationService;
import dk.in2isoft.onlineobjects.ui.Request;
import dk.in2isoft.onlineobjects.util.Messages;

@FacesComponent(value=FooterComponent.FAMILY)
@Dependencies(css={"/core/css/oo_footer.css"},requires={OnlineObjectsComponent.class}, uses = { IconComponent.class })
public class FooterComponent extends AbstractComponent {

	public static final String FAMILY = "onlineobjects.footer";
		
	public FooterComponent() {
		super(FAMILY);
	}

	@Override
	public void restoreState(Object[] state) {
	}

	@Override
	public Object[] saveState() {
		return new Object[] { };
	}
	
	@Override
	protected void encodeBegin(FacesContext context, TagWriter writer) throws IOException {
		
		ConfigurationService configurationService = getBean(ConfigurationService.class);
		Collection<Locale> locales = configurationService.getApplicationLocales(getRequest().getApplication());
		Request request = getRequest();
		writer.startDiv("oo_footer");
		Messages msg = getMessages();
		writer.startP("oo_footer_links");
		if (locales!=null) {
			for (Iterator<Locale> i = locales.iterator(); i.hasNext();) {
				Locale locale = i.next();
				boolean selected = (locale.equals(request.getLocale()));
				if (selected) {
					writer.startStrong();
					writer.text(msg.get(locale.getLanguage(), locale));
					writer.endStrong();
				} else {
					writer.startA("oo_link").withHref(Components.buildLanguageUrl(request, locale)).startSpan();
					writer.text(msg.get(locale.getLanguage(), locale));
					writer.endSpan().endA();
				}
				if (i.hasNext()) {
					writer.startSpan("oo_footer_separator").text(" \u00B7 ").endSpan();
				}
			}
		}
		Locale locale = request.getLocale();
		String agreementsUrl = configurationService.getApplicationContext(AccountController.MOUNT, "agreements", request);
		if (locales!=null && !locales.isEmpty()) {
			writer.startSpan("oo_footer_separator").text(" \u00B7 ").endSpan();
		}
		String signupHref = configurationService.getApplicationContext("account", "signup", request);
		writer.startA("oo_link js-signup").withHref(signupHref).withTestName("footerSignup").startSpan().text(msg.get("sign_up", locale)).endSpan().endA();
		writer.startSpan("oo_footer_separator").text(" \u00B7 ").endSpan();
		String aboutHref = "https://info.onlineobjects.com/" + request.getLanguage() + "/";
		writer.startA("oo_link").withHref(aboutHref).startSpan().text(msg.get("about", locale)).endSpan().endA();
		writer.startSpan("oo_footer_separator").text(" \u00B7 ").endSpan();
		writer.startA("oo_link js-agreements").withHref(agreementsUrl).withTestName("footerAgreements").startSpan().text(msg.get("terms", locale)).endSpan().endA();
		writer.endP();
		writer.startP("oo_footer_logo").startA().withHref("https://www.humanise.dk/").startSpan("oo_icon oo_icon_humanise").endSpan().startStrong().text("Humanise").endStrong().endA().endP();
		writer.endDiv();
	}

	@Override
	protected void encodeEnd(FacesContext context, TagWriter writer) throws IOException {
		
	}

}
