package dk.in2isoft.onlineobjects.modules.information.recognizing;

import nu.xom.Element;

public class TitleRecognizer implements Recognizer {

	@Override
	public String getName() {
		return "title";
	}

	@Override
	public double recognize(Element element) {
		double points = 0;
		if (element.getLocalName().equals("h1"))
			points++;
		return points;
	}

}