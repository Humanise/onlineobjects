package dk.in2isoft.onlineobjects.modules.information.recognizing;

import dk.in2isoft.commons.xml.DOM;
import nu.xom.Element;

public class ActionsRecognizer implements Recognizer {

	@Override
	public String getName() {
		return "action";
	}

	@Override
	public double recognize(Element element) {
		if (element.getChildElements().size() > 0) return 0;
		String[] x = {"log ind", "abonnement", "gå til hovedindhold", "søg", "mail-adresse",
				"adgangskode", "opret konto", "bestil ny adgangskode", "søgefelt", "abonnement",
				"facebook", "twitter", "del", "rss", "del artklen",
				"cookies", "privatlivspolitik",
				"nyhedsbrev",
				"annonce","artiklen fortsætter efter annoncen", "læs også:"
			};
		String text = DOM.getText(element).toLowerCase().trim();
		for (String string : x) {
			
			if (text.equals(string)) {
				return -1;
			}
		}
		// Wikipedia...
		if (DOM.isAny(element, "a")) {
			if (text.equals("edit") || text.equals("^")) {
				return -1;
			}
		}
		if (DOM.isAny(element, "a") && text.equals("edit")) {
			return -1;
		}
		
		String cls = element.getAttributeValue("class");
		if (cls!=null && cls.matches("newsletter")) {
			return -1;
		}
		return 0;
	}

}