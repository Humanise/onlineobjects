package org.onlineobjects.modules.intelligence;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
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
public class Mistral implements LanguageModelHost {

	public class Message {
		String role;
		String content;
	}

	public class Payload {
		String model;
		boolean stream;
		List<Message> messages;

		void addMessage(String content) {
			if (messages == null) {
				messages = new ArrayList();
			}
			var msg = new Message();
			msg.role = "user";
			msg.content = content;
			messages.add(msg);
		}
	}

	public static final String NAME = "mistral";

	private static Logger log = LogManager.getLogger(Mistral.class);
	private ConfigurationService configuration;
	private ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public String name() {
		return NAME;
	}

	@Override
	public void prompt(String prompt, LanguageModel model, OutputStream out) {
		String apiKey = configuration.getMistralApiKey();
		if (Strings.isBlank(apiKey)) {
			throw new IllegalStateException("Missing mistral API key");
		}
		var p = new Payload();
		p.model = model.getId();
		p.addMessage(prompt);
		try (var client = HttpClients.createDefault()) {
			ClassicHttpRequest request = ClassicRequestBuilder.post("https://api.mistral.ai/v1/chat/completions")
					.setEntity(new StringEntity(
							Strings.toJSON(p),
						    ContentType.APPLICATION_JSON))
					.addHeader("Authorization", "Bearer " + apiKey)
		            .build();
			String result = client.execute(request, response -> {
				int code = response.getCode();
				if (code != 200) {
					String body = EntityUtils.toString(response.getEntity());
					log.error("Unexpected response from anthropic, code: " + code + ", body: " + body);
					throw new IOException("Unexpected response code: " + code + ", body: " + body);
				}
				String body = EntityUtils.toString(response.getEntity());
				String r = extract(body);
				return r;
			});
			IOUtils.write(result, out, StandardCharsets.UTF_8);

		} catch (IOException e) {
			log.error(e);
		}
	}

	protected String extract(String json) {
		StringBuilder out = new StringBuilder();
		try {
			JsonNode parsed = objectMapper.readTree(json);
			parsed.path("choices").elements().forEachRemaining(choise -> {
				out.append(choise.path("message").path("content").asText());
			});
		} catch (JsonProcessingException e) {
			log.error(e);
		}
		return out.toString();
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
