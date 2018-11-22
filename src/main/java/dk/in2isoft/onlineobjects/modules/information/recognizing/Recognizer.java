package dk.in2isoft.onlineobjects.modules.information.recognizing;

import java.util.Map;

import nu.xom.Document;
import nu.xom.Element;

public interface Recognizer {
	String getName();

	double recognize(Element element);

	Map<Element, Double> recognize(Document document);
}
