package org.onlineobjects.modules.intelligence;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.annotation.ApplicationScope;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.services.ConfigurationService;

@ApplicationScope
public class Intelligence {

	//private static final String MODEL = "mistral";
	//private static final String MODEL = "llama3.2";
	//private static final String MODEL = "mistral-small";

	private ConfigurationService configuration;
	private Anthropic anthropic;
	private List<LanguageModel> models;

	public Intelligence() {
		models = new ArrayList<>();
		models.add(LanguageModel.of("ollama", "mistral-small", "Mistral small"));
		models.add(LanguageModel.of("ollama", "gpt-oss:20b", "GPT OSS 20b"));
		models.add(LanguageModel.of("ollama", "gemma3:27b", "Gemma 3"));
		models.add(LanguageModel.of("anthropic", "claude-sonnet-4-20250514", "Claude Sonnet").withParameter("version", "2023-06-01"));
	}

	public List<String> synonyms(String word) {
		return List.of(word);
	}

	public List<LanguageModel> getModels() {
		return models;
	}

	private <T> Optional<T> fetch(String path, Object payload, Class<T> type) {
		try (var client = HttpClients.createDefault()) {
			ClassicHttpRequest request = ClassicRequestBuilder.post("http://localhost:11434/api/" + path)
					.setEntity(new StringEntity(
							Strings.toJSON(payload),
						    ContentType.APPLICATION_JSON))
		            .build();
			return client.execute(request, response -> {
				String body = EntityUtils.toString(response.getEntity());
				if (String.class.equals(type)) {
					return (Optional<T>) Optional.of(body);
				}
				return Strings.fromJson(body, type);
			});

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Optional.empty();
		}

	}

	private void stream(String path, Object payload, OutputStream out) {
		try (var client = HttpClients.createDefault()) {
			ClassicHttpRequest request = ClassicRequestBuilder.post("http://localhost:11434/api/" + path)
					.setEntity(new StringEntity(
							Strings.toJSON(payload),
						    ContentType.APPLICATION_JSON))
		            .build();
			client.execute(request, response -> {
				try (BufferedReader reader = new BufferedReader(
	                    new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))) {

	                // Read the response line by line
	                String line;
	                while ((line = reader.readLine()) != null) {
	                    Strings.fromJson(line, StreamResponse.class).ifPresent(r -> {
	                    	if (r.response == null) {
								return;
							}
		                    try {
								out.write(r.response.getBytes());
			                    out.flush();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
	                    });
	                }
	            }
				return null;
			});

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private String getModelName() {
		return "mistral";
		//return "qwq";
	}

	public List<Double> vectorize(String string) {
		Object payload = Map.of("model", "nomic-embed-text", "input", string);
		Optional<EmbeddingsResponse> response = fetch("embed", payload, EmbeddingsResponse.class);
		return response.map(r -> r.embeddings.isEmpty() ? null : r.embeddings.get(0)).orElse(null);
	}

	public String prompt(String prompt) {
		Object payload = Map.of("model", getModelName(), "prompt", prompt);
		Optional<String> response = fetch("generate", payload, String.class);
		return response.orElse(null);
	}


	public void prompt(String prompt, LanguageModel model, OutputStream out) {
		if (model.getProvider().equals("anthropic")) {
			anthropic.prompt(prompt, model, out);
		} else {
			Object payload = Map.of("model", model.getId(), "prompt", prompt);
			stream("generate", payload, out);
		}
	}

	public void prompt(String prompt, OutputStream out) {
		prompt(prompt, getDefaultModel(), out);
	}

	public LanguageModel getDefaultModel() {
		return models.get(0);
	}

	public void summarize(String text, OutputStream out) {
		String prompt = "Help me summarize some text. Only respond with the summary.\n"
				+ "The following is the text to summarize:\n"
				+ text;
		prompt(prompt, out);
	}

	public void keyPoints(String text, OutputStream out) {
		String prompt = "You are an assistant that helps a person. Generate a few key points from some text. Only output the key points as a short list of at most 8 points."
				+ "\n\nThis is the text you should extract the key points from:\n" + text;
		prompt(prompt, out);
	}

	public void author(String text, OutputStream out) {
		String prompt = "You are a function that helps to find the author of a text. "
				+ "You should only state if you are certain about the author. "
				+ "If you cannot detemine the author, simply reply with 'No author'. "
				+ "\n\nWhat is the name of the author of the following text:\n" + StringUtils.abbreviate(text, 1000);
		prompt(prompt, out);
	}

	public static class EmbeddingsResponse {
		public List<List<Double>> embeddings;
	}

	public static class StreamResponse {
		public String response;
	}

	@Autowired
	public void setConfiguration(ConfigurationService configuration) {
		this.configuration = configuration;
	}

	@Autowired
	public void setAnthropic(Anthropic anthropic) {
		this.anthropic = anthropic;
	}

	public Optional<LanguageModel> getModelById(String id) {
		for (LanguageModel model : models) {
			if (id.equals(model.getId())) {
				return Optional.of(model);
			}
		}
		return Optional.empty();
	}
}
