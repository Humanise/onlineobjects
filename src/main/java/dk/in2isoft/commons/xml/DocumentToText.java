package dk.in2isoft.commons.xml;

import java.util.Set;

import com.google.common.collect.Sets;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Text;

public class DocumentToText {

	private Set<String> ignore = Sets.newHashSet("script","head","style","noscript");

	private Set<String> singleBlocks = Sets.newHashSet(
			"br",
			"tr",
			"address",
			"article",
			"aside",
			"canvas",
			"dd",
			"div",
			"dt",
			"fieldset",
			"figcaption",
			"figure",
			"footer",
			"form",
			"header",
			"hr",
			"li",
			"main",
			"nav",
			"noscript",
			"output",
			"pre",
			"section",
			"table",
			"tfoot",
			"video");

	private Set<String> doubleBlocks = Sets.newHashSet("p","h1","h2","h3","h4","h5","h6","ol","ul","dl","blockquote");

	private int newLines = 0;

    public String getText(Document doc) {
        StringBuffer data = new StringBuffer();
        traverse(doc,data);
        String text = data.toString();
        return clean(text);
    }

    public String clean(String text) {
    	String whitespace_chars =  ""       /* dummy empty string for homogeneity */
                + "\\u0009" // CHARACTER TABULATION
                //+ "\\u000A" // LINE FEED (LF)
                + "\\u000B" // LINE TABULATION
                + "\\u000C" // FORM FEED (FF)
                + "\\u000D" // CARRIAGE RETURN (CR)
                + "\\u0020" // SPACE
                + "\\u0085" // NEXT LINE (NEL)
                + "\\u00A0" // NO-BREAK SPACE
                + "\\u1680" // OGHAM SPACE MARK
                + "\\u180E" // MONGOLIAN VOWEL SEPARATOR
                + "\\u2000" // EN QUAD
                + "\\u2001" // EM QUAD
                + "\\u2002" // EN SPACE
                + "\\u2003" // EM SPACE
                + "\\u2004" // THREE-PER-EM SPACE
                + "\\u2005" // FOUR-PER-EM SPACE
                + "\\u2006" // SIX-PER-EM SPACE
                + "\\u2007" // FIGURE SPACE
                + "\\u2008" // PUNCTUATION SPACE
                + "\\u2009" // THIN SPACE
                + "\\u200A" // HAIR SPACE
                + "\\u2028" // LINE SEPARATOR
                + "\\u2029" // PARAGRAPH SEPARATOR
                + "\\u202F" // NARROW NO-BREAK SPACE
                + "\\u205F" // MEDIUM MATHEMATICAL SPACE
                + "\\u3000" // IDEOGRAPHIC SPACE
                ;
    	// Remove trailing and leading
        text = text.replaceAll("(?m)["+whitespace_chars+"]+$", "").replaceAll("(?m)^["+whitespace_chars+"]+", "");
        // Normalize to common space
        text = text.replaceAll("["+whitespace_chars+"]+", " ");
        // Remove leading spaces/lines
        text = text.replaceAll("^[ \\n]+", "");
        // Remove trailing
        text = text.replaceAll(" \\n", "\n");
        // Max 2 breaks
        text = text.replaceAll("([\\n]{2,})", "\n\n");
        // Max 1 space
        //text = text.replaceAll("["+whitespace_chars+"]{2,}", " ");
        return text;
    }

    private void traverse(nu.xom.Node parent, StringBuffer data) {
    	if (parent==null) return;
        int count = parent.getChildCount();
        for (int i=0;i<count;i++) {
            nu.xom.Node node = parent.getChild(i);
            if (node instanceof Text) {
                String value = node.getValue();
                if (value.length() > 0) {
                	while (newLines > 0) {
                		data.append("\n");
                		newLines--;
                	}
                    value = value.replaceAll("\\t"," ");
                    value = value.replaceAll("\n", " ");
                    value = value.replaceAll("\\s{2,}", " ");
                    //value = value.replaceAll("\\s{2,}", " ");
    				data.append(value);
                }
            }
            else {
            	if (node instanceof Element) {
            		Element element = (Element) node;
            		String name = element.getLocalName().toLowerCase();
            		if (data.length() > 0) {
            			if (singleBlocks.contains(name)) {
            				newLines = Math.max(newLines, 1);
            			}
            			if (doubleBlocks.contains(name)) {
            				newLines = Math.max(newLines, 2);
            			}
            		}
					if (!ignore.contains(name)) {
                        traverse(node,data);
            		}
            		if (data.length() > 0) {
            			if (singleBlocks.contains(name)) {
            				newLines = Math.max(newLines, 1);
            			}
            			if (doubleBlocks.contains(name)) {
            				newLines = Math.max(newLines, 2);
            			}
            		}
            	}
            }
        }

    }
}
