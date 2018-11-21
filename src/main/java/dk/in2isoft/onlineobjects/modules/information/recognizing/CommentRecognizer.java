package dk.in2isoft.onlineobjects.modules.information.recognizing;

import dk.in2isoft.commons.xml.DOM;
import nu.xom.Element;

public class CommentRecognizer implements Recognizer {

	@Override
	public String getName() {
		return "comment";
	}

	@Override
	public double recognize(Element element) {
		if (DOM.isAny(element, "html","head","body")) {
			return 0;
		}
		String cls = element.getAttributeValue("class");
		if (cls!=null && cls.toLowerCase().indexOf("comment") != -1) {
			return -1;
		}
		return 0;
	}

}