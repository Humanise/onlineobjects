package dk.in2isoft.onlineobjects.modules.information.recognizing;

import java.util.Map;

import dk.in2isoft.commons.xml.DOM;
import nu.xom.Document;
import nu.xom.Element;

public class ActionsRecognizer implements Recognizer {

	@Override
	public String getName() {
		return "action";
	}

	@Override
	public double recognize(Element element) {
		if (element.getChildElements().size() > 0) return 0;
		String[] texts = {
				"log ind", "abonnement", "gå til hovedindhold", "søg", "mail-adresse",
				"adgangskode", "opret konto", "bestil ny adgangskode", "søgefelt", "abonnement",
				"facebook", "twitter", "linkedin","reddit","pocket","flipboard","reddit",
				"del", "rss", "del artklen",
				"cookies", "privatlivspolitik","kopiér link", "kopier link", "tilmeld",
				"nyhedsbrev",
				"annonce","artiklen fortsætter efter annoncen", "læs også:",
				"læs artiklen senere","læs kommentar",
				"subscribe", "next article", "most recent newsletter", "pdf version", "menu", "skip to main content",
				"search", "sign up", "open main menu",
				"read later",
				"e-alert", "submit", "my account", "login", "☰",
				"tweet","share","pin it",
				"share on facebook","share on twitter","share on reddit","share on whatsapp","share on google+","share by email",
				"share to twitter","blogthis!","share to facebook","share to pinterest","email this",
				"follow me on twitter","click to follow"
			};
		String text = DOM.getText(element).toLowerCase().trim();
		for (String string : texts) {
			
			if (text.equals(string)) {
				return -1;
			}
		}
		// Wikipedia...
		if (DOM.isAny(element, "a")) {
			String href = element.getAttributeValue("href");
			if (text.equals("edit")) {
				return -1;
			}
			if (text.equals("top") || text.equals("to top")) {
				if (href!=null && href.startsWith("#")) {
					return -1;
				}
			}
		}
		
		String cls = element.getAttributeValue("class");
		if (cls!=null && cls.matches("newsletter")) {
			return -1;
		}
		return 0;
	}

	@Override
	public Map<Element,Double> recognize(Document document) {
		return null;
	}

}