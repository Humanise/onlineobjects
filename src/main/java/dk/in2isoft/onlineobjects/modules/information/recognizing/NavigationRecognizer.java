package dk.in2isoft.onlineobjects.modules.information.recognizing;

import nu.xom.Element;

public class NavigationRecognizer implements Recognizer {

	@Override
	public String getName() {
		return "navigation";
	}

	@Override
	public double recognize(Element element) {
		if (element.getLocalName().equals("nav")) {
			return -1;
		}
		String cls = element.getAttributeValue("class");
		if (cls!=null && cls.toLowerCase().matches("nav|menu")) {
			return -1;
		}
		return 0;
	}

}