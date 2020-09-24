package dk.in2isoft.onlineobjects.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.modules.networking.NetworkResponse;
import dk.in2isoft.onlineobjects.modules.networking.NetworkService;

public class TestableNetworkService extends NetworkService {

	@Autowired
	private ApplicationContext context;

	public TestableNetworkService() {
	}

	@Override
	public NetworkResponse getSilently(String url) {
		throw new IllegalStateException("Not implemented!");
		// - return super.getSilently(url);
	}
	
	@Override
	public NetworkResponse get(URI uri) throws IOException {
		if (uri.getScheme().equals("file") || true) {
			return super.get(uri);
		}
		NetworkResponse response = new NetworkResponse();
		String fileName = uri.toString().replaceAll("[\\W]+", "_");
		Resource resource = context.getResource("urls/" + fileName);
		if (!resource.exists()) {
			throw new IllegalStateException("Dummy response not found: " +uri.toString() + " | " + fileName);
		}
		InputStream input = resource.getInputStream();
		File file = File.createTempFile("networkservice", "tmp");
		file.deleteOnExit();
		FileOutputStream output = new FileOutputStream(file);
		IOUtils.copy(input, output);
		response.setUri(uri);
		response.setFile(file);
		response.setMimeType("text/html");
		response.setEncoding(Strings.UTF8);
		return response;
	}
}
