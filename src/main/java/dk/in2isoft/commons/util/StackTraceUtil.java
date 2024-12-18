package dk.in2isoft.commons.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

/**
 * Simple utilities to return the stack trace of an exception as a String.
 */
public final class StackTraceUtil {

	public static String getStackTrace(Throwable aThrowable) {
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		aThrowable.printStackTrace(printWriter);
		return result.toString();
	}

	/**
	 * Defines a custom format for the stack trace as String.
	 */
	public static String getCustomStackTrace(Throwable aThrowable) {
		// add the class name and any message passed to constructor
		final StringBuilder result = new StringBuilder("BOO-BOO: ");
		result.append(aThrowable.toString());
		final String NEW_LINE = System.getProperty("line.separator");
		result.append(NEW_LINE);

		// add each element of the stack trace
		final List<StackTraceElement> traceElements = Arrays.asList(aThrowable.getStackTrace());
		for (StackTraceElement element : traceElements) {
			result.append(element);
			result.append(NEW_LINE);
		}
		return result.toString();
	}
}