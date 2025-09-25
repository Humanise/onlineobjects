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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.annotation.ApplicationScope;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.services.ConfigurationService;

@ApplicationScope
public class Anthropic {

	private ConfigurationService configuration;

	public void prompt(String prompt, OutputStream out) {
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
					.addHeader("anthropic-version", "2023-06-01")
					.addHeader("x-api-key", apiKey)
		            .build();
			client.execute(request, response -> {
				int code = response.getCode();
				if (code != 200) {
					String body = new BasicHttpClientResponseHandler().handleResponse(response);
					throw new IOException("Unexpected response code: " + code + ", body: " + body);
					//return null;
				}
				try (BufferedReader reader = new BufferedReader(
	                    new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))) {
					StringBuilder buffer = new StringBuilder();
	                // Read the response line by line
	                String line;
	                String event = null;
	                String data = null;
	                while ((line = reader.readLine()) != null) {
	                	System.out.println(System.currentTimeMillis());
	                	if (line.startsWith("event: ")) {
	                		event = line.substring(7);
	                	} else if (line.startsWith("data: ")) {
	                		data = line.substring(6);
	                	} else {
		                	System.out.println(event + " | " + data);
		                	if ("content_block_delta".equals(event)) {
			                	Map<?,?> parsed = Strings.fromJson(data, Map.class).orElse(Map.of());
		                		Object delta = parsed.get("delta");
		                		if (delta instanceof Map) {
		                			Object text = ((Map<?,?>) delta).get("text");
		                			if (text instanceof String) {
		        	                	out.write(((String)text).getBytes());
		        	                	out.flush();
		                			}
		                		}
		                	}
	                	}
	                	buffer.append(line);
	                }
	            }
				return null;
			});

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Autowired
	public void setConfiguration(ConfigurationService configuration) {
		this.configuration = configuration;
	}
}
