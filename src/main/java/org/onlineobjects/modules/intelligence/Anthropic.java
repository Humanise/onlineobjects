package org.onlineobjects.modules.intelligence;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.annotation.ApplicationScope;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.services.ConfigurationService;

@ApplicationScope
public class Anthropic implements LanguageModelHost {

	private static Logger log = LogManager.getLogger(Anthropic.class);
	private ConfigurationService configuration;
	private ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public String name() {
		return "anthropic";
	}

	@Override
	public void prompt(String prompt, LanguageModel model, OutputStream out) {
		String version = model.getParameters().get("version");
		var payload = Map.of(
			"model", "claude-sonnet-4-20250514",
			"max_tokens", 1024,
			"stream", true,
			"messages", List.of(Map.of("role", "user", "content", prompt))
		);
		String apiKey = configuration.getAnthropicApiKey();
		if (Strings.isBlank(apiKey)) {
			throw new IllegalStateException("Missing anthropic API key");
		}
		try (var client = HttpClients.createDefault()) {
			ClassicHttpRequest request = ClassicRequestBuilder.post("https://api.anthropic.com/v1/messages")
					.setEntity(new StringEntity(
							Strings.toJSON(payload),
						    ContentType.APPLICATION_JSON))
					.addHeader("anthropic-version", version)
					.addHeader("x-api-key", apiKey)
		            .build();
			client.execute(request, response -> {
				int code = response.getCode();
				if (code != 200) {
					String body = new BasicHttpClientResponseHandler().handleResponse(response);
					log.error("Unexpected response from anthropic, code: " + code + ", body: " + body);
					throw new IOException("Unexpected response code: " + code + ", body: " + body);
				}
				try (BufferedReader reader = new BufferedReader(
	                    new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))) {
	                String line;
	                String event = null;
	                String data = null;
	                while ((line = reader.readLine()) != null) {
	                	if (line.startsWith("event: ")) {
	                		event = line.substring(7);
	                	} else if (line.startsWith("data: ")) {
	                		data = line.substring(6);
		                	if ("content_block_delta".equals(event)) {
			                	handleContent(data, out);
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

	private void handleContent(String data, OutputStream out) throws IOException {
		Map<?,?> parsed = Strings.fromJson(data, Map.class).orElseThrow();
		Object delta = parsed.get("delta");
		if (delta instanceof Map) {
			Object text = ((Map<?,?>) delta).get("text");
			Object type = ((Map<?,?>) delta).get("type");
			if ("text_delta".equals(type)) {
				if (text instanceof String) {
		        	out.write(((String)text).getBytes());
		        	out.flush();
				}
			}
		}
	}

	protected String extract(String json) {
		try {
			JsonNode parsed = objectMapper.readTree(json);
			return parsed.path("delta").path("text").asText();
		} catch (JsonProcessingException e) {
			log.error(e);
		}
		return null;
	}

	@Override
	public boolean isConfigured() {
		return Strings.isNotBlank(configuration.getAnthropicApiKey());
	}

	@Autowired
	public void setConfiguration(ConfigurationService configuration) {
		this.configuration = configuration;
	}
}
