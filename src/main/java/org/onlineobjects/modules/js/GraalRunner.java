package org.onlineobjects.modules.js;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class GraalRunner {

	public Object execute(String code) throws ScriptException {
		code = "(function() {" + code + "})()";
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("Graal.js");
		return engine.eval(code);
	}

	public Object execute(File[] files, String code) throws ScriptException, IOException {
		code = "(function() {" + code + "})()";
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("Graal.js");
		for (File file : files) {
			try (var reader = new FileReader(file)) {
				engine.eval(reader);				
			}
		}
		return engine.eval(code);
	}
}
