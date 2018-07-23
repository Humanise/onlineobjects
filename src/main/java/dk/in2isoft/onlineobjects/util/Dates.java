package dk.in2isoft.onlineobjects.util;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Dates {
	
	private static Map<String,String> LONG = new HashMap<String, String>();
	private static Map<String,String> SHORT = new HashMap<String, String>();
	private static Map<String,String> DATEWITHTIME = new HashMap<String, String>();
	
	static {
		LONG.put("en", "EEEE MMMM d. yyyy 'at' HH:mm:ss");
		LONG.put("da", "EEEE 'd.' d. MMMM yyyy 'kl.' HH:mm:ss");

		DATEWITHTIME.put("en", "MMMM d. yyyy 'at' HH:mm:ss");
		DATEWITHTIME.put("da", "d. MMMM yyyy 'kl.' HH:mm:ss");

		SHORT.put("en", "MMMM d. yyyy");
		SHORT.put("da", "d. MMMM yyyy");
	}

	public static String formatLongDate(Date date, Locale locale) {
		if (date==null) {
			return "";
		}
		SimpleDateFormat format = new SimpleDateFormat(LONG.get(locale.getLanguage()),locale);
		return format.format(date);
	}

	public static String formatShortDate(Date date, Locale locale) {
		if (date==null) {
			return "";
		}
		SimpleDateFormat format = new SimpleDateFormat(SHORT.get(locale.getLanguage()),locale);
		return format.format(date);
	}

	public static String formatDateWithTime(Date date, Locale locale) {
		if (date==null) {
			return "";
		}
		SimpleDateFormat format = new SimpleDateFormat(DATEWITHTIME.get(locale.getLanguage()),locale);
		return format.format(date);
	}

	public static String formatTime(Date date, Locale locale) {
		if (date==null) {
			return "";
		}
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss",locale);
		return format.format(date);
	}

	public static String formatDate(Date value, boolean weekday, boolean time, Locale locale) {
		if (value==null) {
			return "";
		}
		if (!weekday && time) {
			return formatDateWithTime(value, locale);
		}
		if (weekday && time) {
			return formatLongDate(value, locale);
		}
		return formatShortDate(value, locale);
	}

	public static String formatDurationFromNow(Date date) {
		return format((System.currentTimeMillis() - date.getTime())/1000);
	}

	public static String format(Duration duration) {		
		return format(duration.getSeconds());
	}

	public static String format(long seconds) {		
		
		long secs = seconds % 60;
		
		long minutes = (seconds / 60) % 60;

		long hours = (seconds / 60 / 60) % 24;

		long days = (seconds / 60 / 60 / 24) % 365;
		List<String> parts = new ArrayList<>();
		if (days > 0) {
			parts.add(days + " days");
		}
		if (hours > 0) {
			parts.add(hours + " hours");
		}
		if (minutes > 0) {
			parts.add(minutes + " minutes");
		}
		if (secs > 0) {
			parts.add(secs + " seconds");
		}
		return String.join(", ", parts);
	}
}
