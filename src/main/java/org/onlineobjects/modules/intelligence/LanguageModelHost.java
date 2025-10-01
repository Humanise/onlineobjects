package org.onlineobjects.modules.intelligence;

import java.io.OutputStream;

public interface LanguageModelHost {

	void prompt(String prompt, LanguageModel model, OutputStream out);

	String name();

	boolean isConfigured();
}