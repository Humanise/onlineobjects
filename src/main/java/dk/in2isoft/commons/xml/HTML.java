package dk.in2isoft.commons.xml;

import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;

import com.google.common.collect.Sets;

import dk.in2isoft.commons.lang.Strings;

public class HTML {

	public static Set<String> INLINE_TEXT = Sets.newHashSet("a", "abbr", "b", "bdi", "bdo",
			"br", "cite", "code", "data", "del", "dfn", "em", "i", "ins", "kbd", "mark", "q",
			"rp", "rt", "rtc", "ruby", "s", "samp", "small", "span", "strong",
			"sub", "sup", "time", "u", "var", "wbr");
	
	public static String escape(String str) {
		str = Strings.stripNonValidXMLCharacters(str);
		return StringEscapeUtils.escapeXml(str);
	}
}
