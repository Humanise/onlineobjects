package dk.in2isoft.in2igui.data;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.lang.StringEscapeUtils;

import dk.in2isoft.commons.lang.Strings;

public abstract class MarkupWriter {

	protected PrintWriter writer;
	boolean open = false;
		
	protected void close() {
		if (open) {
			writer.write(">");
			open = false;
		}
	}

	protected void text(String str) {
		close();
		try {
			str = Strings.stripNonValidXMLCharacters(str);			
			StringEscapeUtils.escapeXml(writer,str);
		} catch (IOException e) {
			// Ignore
		}
	}

	protected void raw(String markup) {
		close();
		writer.write(markup);
	}

	protected void startTag(String tag) {
		close();
		writer.write("<");
		writer.write(tag);
		open = true;
	}
	
	protected void endTag(String tag) {
		close();
		writer.write("</");
		writer.write(tag);
		writer.write(">");
	}

	protected void withAttribute(String name, Object value) {
		if (value!=null && name!=null) {
			writer.write(" "+name+"=\"");
			writer.write(StringEscapeUtils.escapeXml(value.toString()));
			writer.write("\"");
		}
	}

}
