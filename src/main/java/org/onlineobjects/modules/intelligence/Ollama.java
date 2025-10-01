package org.onlineobjects.modules.intelligence;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.onlineobjects.modules.intelligence.Intelligence.StreamResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.annotation.ApplicationScope;

import com.fasterxml.jackson.databind.ObjectMapper;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.services.ConfigurationService;

@ApplicationScope
public class Ollama implements LanguageModelHost {

	private static Logger log = LogManager.getLogger(Ollama.class);
	private ConfigurationService configuration;
	private ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public String name() {
		return "ollama";
	}
	@Override
	public void prompt(String prompt, LanguageModel model, OutputStream out) {
		Object payload = Map.of("model", model.getId(), "prompt", prompt);
		try (var client = HttpClients.createDefault()) {
			ClassicHttpRequest request = ClassicRequestBuilder.post("http://localhost:11434/api/generate")
					.setEntity(new StringEntity(
							Strings.toJSON(payload),
						    ContentType.APPLICATION_JSON))
		            .build();
			client.execute(request, response -> {
				try (BufferedReader reader = new BufferedReader(
	                    new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))) {

	                // Read the response line by line
	                String line;
	                boolean failed = false;
	                while (!failed && (line = reader.readLine()) != null) {
	                	var parsed = Strings.fromJson(line, StreamResponse.class);
	                	if (parsed.isPresent()) {
	                		var r = parsed.get();
	                    	if (r.response != null) {
			                    try {
									out.write(r.response.getBytes());
				                    out.flush();
								} catch (IOException e) {
									failed = true;
									log.error(e);
								}
							}
	                	}
	                }
	            }
				return null;
			});

		} catch (IOException e) {
			log.error(e);
		}
	}


	@Autowired
	public void setConfiguration(ConfigurationService configuration) {
		this.configuration = configuration;
	}
}
