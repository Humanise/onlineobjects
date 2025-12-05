package dk.in2isoft.onlineobjects.core;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Path {

	String[] exactly() default {};

	String expression() default "";

	String of() default "";

	Method method() default Method.NONE;

	public static enum Method {
		GET, POST, DELETE, NONE
	}
}
