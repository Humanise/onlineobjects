package dk.in2isoft.commons.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractCommandLineInterface {

	private static Logger log = LogManager.getLogger(AbstractCommandLineInterface.class);
	
	protected synchronized String execute(String cmd) throws IOException {
		try {
			Process p = Runtime.getRuntime().exec(cmd,new String[] {"PATH=/opt/local/bin:/opt/local/sbin:/bin:/sbin:/usr/bin:/usr/local/bin:/usr/sbin"});
			int exitCode = p.waitFor();
			checkError(p,cmd, exitCode);
			return getResult(p);
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
	}

	private void checkError(Process p, String cmd, int exitCode) throws IOException, InterruptedException {
		InputStream s = p.getErrorStream();
		int c;
		StringWriter sw = new StringWriter();
		while ((c = s.read()) != -1) {
			sw.write(c);
		}
		if (sw.getBuffer().length()>0) {
			log.warn(sw.getBuffer().toString());
			log.warn("command: "+cmd);
		}
		if (exitCode!=0) {
			log.warn("exitCode: " + exitCode + ", for command:" + cmd);
			throw new IOException(sw.toString());
		}
	}

	private String getResult(Process p) throws IOException {
		InputStream s = p.getInputStream();
		int c;
		StringWriter sw = new StringWriter();
		while ((c = s.read()) != -1) {
			sw.write(c);
		}
		return sw.toString();
	}

}