package dk.in2isoft.onlineobjects.core;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Path {

	String[] start() default {};
	
	String[] exactly() default {};
	
	String expression() default "";
}
