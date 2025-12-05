package dk.in2isoft.onlineobjects.util;

import java.net.URI;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;

import dk.in2isoft.commons.lang.Strings;

public class ValidationUtil {

	//private static final Pattern PASSWORD = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");

	public static boolean isWellFormedEmail(String email) {
		if (email==null || !email.equals(email.trim())) {
			return false;
		}
		return EmailValidator.getInstance(false, false).isValid(email);
	}

	public static boolean isValidUsername(String username) {
		if (Strings.isBlank(username)) {
			return false;
		}
		if (username.length()<2) {
			return false;
		}
		if (Strings.isInteger(username)) {
			return false;
		}
		if ("core".equals(username) || "app".equals(username) || "service".equals(username) || "dwr".equals(username) || "hui".equals(username)) {
			return false;
		}
		return StringUtils.containsOnly(username, "abcdefghijklmnopqrstuvwxyz0123456789");
	}

	public static boolean isValidPassword(String password) {
		if (password==null) {
			return false;
		}
		for (int i = 0; i < password.length(); i++) {
			char character = password.charAt(i);
			if (Character.isWhitespace(character)) {
				return false;
			}
		}
		if (password.chars().distinct().count() < 2) {
			return false;
		}
		return password.length() > 7;
		//return PASSWORD.matcher(password).matches();
	}

	public static boolean isWellFormedURI(String str) {
		try {
			URI.create(str);
			return true;
		} catch (Exception e) {

		}
		return false;
	}
}
