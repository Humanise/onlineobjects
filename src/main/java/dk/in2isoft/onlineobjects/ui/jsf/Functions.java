package dk.in2isoft.onlineobjects.ui.jsf;

import java.util.Date;
import java.util.Locale;

import javax.faces.context.FacesContext;

import org.apache.commons.lang.StringUtils;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.util.Dates;

public class Functions {

	public static String json(Object object) {
		return Strings.toJSON(object);
	}

	public static String join(String one, String two) {
		return StringUtils.join(new String[] {one,two}, "");
	}

	public static String formatDate(Object value, String format) {
		Date date = null;
		if (value instanceof Date) {
			date = (Date) value;
		}
		else if (value instanceof Number) {
			Number number = (Number) value;
			date = new Date(number.longValue());
		}
		if (date!=null && "long".equals(format)) {
			return Dates.formatLongDate(date, getLocale());
		}
		if (date!=null && "short".equals(format)) {
			return Dates.formatShortDate(date, getLocale());
		}
		
		return value.toString();
	}

	private static Locale getLocale() {
		return FacesContext.getCurrentInstance().getViewRoot().getLocale();
	}
}
