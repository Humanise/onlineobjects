package dk.in2isoft.onlineobjects.core;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface View {

	String[] ui() default {};
	String jsf() default "";

}
