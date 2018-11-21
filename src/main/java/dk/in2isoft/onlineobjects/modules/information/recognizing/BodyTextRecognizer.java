package dk.in2isoft.onlineobjects.modules.information.recognizing;

import java.util.HashSet;

import com.google.common.collect.Sets;

import dk.in2isoft.commons.xml.DOM;
import nu.xom.Element;

public class BodyTextRecognizer implements Recognizer {

	private HashSet<String> inlines;

	public BodyTextRecognizer() {
		inlines = Sets.newHashSet("strong", "em", "a", "img", "br", "hr", "sub", "sup", "abbr", "dfn", "del", "ins",
				"cite", "q", "code", "tt", "kbd", "samp", "var", "time");
	}

	@Override
	public String getName() {
		return "text";
	}

	@Override
	public double recognize(Element element) {
		if (element.getLocalName().equals("p")) {
			return scoreByTextLength(element);
		}
		if (element.getLocalName().equals("div")) {
			boolean hasOnlyInline = DOM.findElement(element, other -> {
				return !inlines.contains(other.getLocalName());
			}) == null;
			if (hasOnlyInline) {
				return scoreByTextLength(element);
			}
		}
		return 0;
	}

	private double scoreByTextLength(Element element) {
		double x = ((double) DOM.getText(element).length()) / 100.0;
		return Math.min(1, x);
	}

}