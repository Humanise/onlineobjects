package dk.in2isoft.onlineobjects.test.hui;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.script.ScriptException;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.onlineobjects.modules.js.GraalRunner;
import org.onlineobjects.modules.js.NodeRunner;

public class TestJavaScript {

	@Test
	public void testGraal() throws ScriptException {
		var code = """
				return {abe: 1}
				""";

		Object result = new GraalRunner().execute(code);
		assertEquals(1, ((Map<?,?>)result).get("abe"));
	}

	@Test
	public void testGraa2l() throws Exception {
		var hmd = """
				# hey

				[ ] asdasdsada
				""";
		var code = """
				var doc = new DocumentParser().parse(\"%s\");
				return new DocumentRenderer().toHTML(doc);
				""";
		code = code.formatted(escapeJS(hmd));
		File[] dependencies = {new File("/Users/jobm/Code/humanise/onlineobjects/src/main/webapp/apps/developer/documents/document-model.js")};
		{
			Object result = new GraalRunner().execute(dependencies, code);
			System.out.println(result);
		}
		{
			Object result = new NodeRunner().execute(dependencies, code);
			System.out.println(result);
		}
	}

	private String escapeJS(String str) {
		return str.replaceAll("\n", "\\\\n").replaceAll("\"", "\\\"");
	}

	@Test
	public void testNode() throws IOException {
		ProcessBuilder processBuilder = new ProcessBuilder();
		Map<String, String> environment = processBuilder.environment();
		//environment.put("PATH","/opt/local/bin:/opt/local/sbin:/bin:/sbin:/usr/bin:/usr/local/bin:/usr/sbin");
		//processBuilder.command("echo", "$PATH");
		//String file = getClass().getResource("document-model.js").getFile();
		String file = "/Users/jobm/Code/humanise/onlineobjects/src/main/webapp/apps/developer/documents/document-model.js";
		var hmd = """
				# hey

				[ ] asdasdsada
				""";
		var code = """
				var doc = new parsing.DocumentParser().parse('%s');
				return JSON.stringify(doc, null, 2);
				""";
		code = code.formatted(hmd.replaceAll("\n", "\\n")).replaceAll("\n", "");
		String js = "const parsing = require('/Users/jobm/Code/humanise/onlineobjects/src/main/webapp/apps/developer/documents/document-model.js'); console.log((function() {" + code + "})());";

		List<String> commands = new ArrayList<>();
		commands.add("/opt/local/bin/node");
		//commands.add("-r");
		//commands.add("/Users/jobm/Code/humanise/onlineobjects/src/main/webapp/apps/developer/documents/document-model.js");
		commands.add("-e");
		commands.add(js);
		processBuilder.command(commands);
		Process process = processBuilder.start();
		String version = IOUtils.toString(process.getInputStream(), Charset.defaultCharset());
		String error = IOUtils.toString(process.getErrorStream(), Charset.defaultCharset());
		//System.out.println(commands.stream().collect(Collectors.joining(" ")));
		System.out.println(version);
		System.out.println(error);
		new ArrayList<String>().stream().toList();

	}
}
