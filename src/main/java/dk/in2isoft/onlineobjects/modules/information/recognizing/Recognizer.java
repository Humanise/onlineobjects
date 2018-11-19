package dk.in2isoft.onlineobjects.modules.information.recognizing;

import nu.xom.Element;

public interface Recognizer {
	String getName();

	double recognize(Element element);
}
