package dk.in2isoft.onlineobjects.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dk.in2isoft.commons.lang.Files;
import dk.in2isoft.commons.util.AbstractCommandLineInterface;
import dk.in2isoft.onlineobjects.services.ConfigurationService;

public class UglifyScriptCompressor extends AbstractCommandLineInterface implements ScriptCompressor {

	private static final Logger log = LogManager.getLogger(UglifyScriptCompressor.class);

	private ConfigurationService configuration;

	@Override
	public String compress(String js) {
		try {
			File tempFile = File.createTempFile("uglifyin", ".js");
			Files.overwriteTextFile(js, tempFile);
			return execute(configuration.getUglifyPath() + " " + tempFile.getAbsolutePath() + " -c -m");
		} catch (IOException e) {
			log.error("Unable to compress js", e);
		}
		return js;
	}

	@Override
	public void compress(Reader in, Writer out) throws IOException {

		try {
			File tempFile = File.createTempFile("uglifyin", ".js");
			File outFile = File.createTempFile("uglifyout", ".js");
			try (FileOutputStream output = new FileOutputStream(tempFile)) {
				IOUtils.copy(in, output, StandardCharsets.UTF_8);
			}
			String cmd = configuration.getUglifyPath() + " " + tempFile.getAbsolutePath() + " -o " + outFile.getAbsolutePath() + " -c -m";
			execute(cmd);
			try (FileReader reader = new FileReader(outFile)) {
				IOUtils.copy(reader, out);
			}
		} catch (IOException e) {
			log.error("Unable to compress js", e);
			if (in.markSupported()) {
				in.reset();
			}
			IOUtils.copy(in, out);
		}
	}

	public void setConfiguration(ConfigurationService configuration) {
		this.configuration = configuration;
	}
}
