package org.onlineobjects.modules.intelligence;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.annotation.ApplicationScope;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.services.ConfigurationService;

@ApplicationScope
public class OllamaCloud implements LanguageModelHost {

	private static Logger log = LogManager.getLogger(OllamaCloud.class);
	private ConfigurationService configuration;

	@Override
	public String name() {
		return "ollama-cloud";
	}

	@Override
	public void prompt(String prompt, LanguageModel model, OutputStream out) {
		if (!isConfigured()) {
			throw new IllegalStateException("Ollama base url not configured");
		}
		Object payload = Map.of("model", model.getId(), "messages", List.of(Map.of("role", "user", "content", prompt)));
		try (var client = HttpClients.createDefault()) {
			ClassicHttpRequest request = ClassicRequestBuilder.post(getBaseUrl() + "/chat")
					.setEntity(new StringEntity(
							Strings.toJSON(payload),
						    ContentType.APPLICATION_JSON))
					.addHeader("Authorization", "Bearer " + configuration.getOllamaCloudApiKey())
		            .build();
			client.execute(request, response -> {
				int code = response.getCode();
				if (code != 200) {
					throw new IOException("Not 200");
				}
				try (BufferedReader reader = new BufferedReader(
	                    new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))) {

	                // Read the response line by line
	                String line;
	                boolean failed = false;
	                while (!failed && (line = reader.readLine()) != null) {
	                	var parsed = Strings.fromJson(line, StreamResponse.class);
	                	if (parsed.isPresent()) {
	                		var r = parsed.get();
	                    	if (r.message != null && r.message.content != null) {
			                    try {
									out.write(r.message.content.getBytes());
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

	private String getBaseUrl() {
		return configuration.getOllamaCloudUrl();
	}

	@Override
	public boolean isConfigured() {
		return Strings.isNotBlank(getBaseUrl()) && Strings.isNotBlank(configuration.getOllamaCloudApiKey());
	}

	@Autowired
	public void setConfiguration(ConfigurationService configuration) {
		this.configuration = configuration;
	}

	public static class StreamResponse {
		public Message message;
	}

	public class Message {
		public String content;
	}

}
