package dk.in2isoft.onlineobjects.modules.information.recognizing;

import nu.xom.Element;

public class AsideRecognizer implements Recognizer {

	@Override
	public String getName() {
		return "aside";
	}

	@Override
	public double recognize(Element element) {
		if (element.getLocalName().equals("aside")) {
			return -1;
		}
		String cls = element.getAttributeValue("class");
		if (cls!=null && cls.toLowerCase().matches("sidebar|aside|side\\-bar")) {
			return -1;
		}
		return 0;
	}

}