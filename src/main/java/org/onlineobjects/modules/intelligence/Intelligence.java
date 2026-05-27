package org.onlineobjects.modules.intelligence;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.context.annotation.ApplicationScope;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.services.ConfigurationService;
import jakarta.servlet.ServletOutputStream;

@ApplicationScope
public class Intelligence  implements ApplicationListener<ContextRefreshedEvent> {

	private static Logger log = LogManager.getLogger(Intelligence.class);
	private ConfigurationService configuration;
	private List<LanguageModel> models;
	private List<LanguageModelHost> hosts;
	private List<LanguageModel> availableModels;
	private List<Embedder> embedders;
	private LanguageModel defaultModel;

	public Intelligence() {
		models = new ArrayList<>();
		models.add(LanguageModel.of("ollama", "mistral-small", "Mistral small"));
		models.add(LanguageModel.of("ollama", "mistral", "Mistral"));
		models.add(LanguageModel.of("ollama", "gpt-oss:20b", "GPT OSS 20b"));
		models.add(LanguageModel.of("ollama", "gemma3:27b", "Gemma 3 - 27b"));
		models.add(LanguageModel.of("ollama", "gemma3:12b", "Gemma 3 - 12b"));
		models.add(LanguageModel.of("ollama", "gemma4:e4b", "Gemma 4 - e4b"));
		models.add(LanguageModel.of("ollama", "qwen3:30b", "Qwen 3 - 30b"));
		models.add(LanguageModel.of("ollama", "qwen3.5:9b", "Qwen 3.5 - 9b"));

		models.add(LanguageModel.of("anthropic", "claude-sonnet-4-20250514", "Claude Sonnet").withParameter("version", "2023-06-01"));

		models.add(LanguageModel.of("ollama-cloud", "gpt-oss:120b", "GPT OSS 120b"));

	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		availableModels = hosts.stream().filter(h -> h.isConfigured()).flatMap(h -> models.stream().filter(m -> m.getProvider().equals(h.name()))).toList();

		String model = configuration.getLanguageModel();
		if (Strings.isNotBlank(model)) {
			for (LanguageModel languageModel : availableModels) {
				if (model.equals(languageModel.getId())) {
					defaultModel = languageModel;
				}
			}
		}
		if (defaultModel == null && !availableModels.isEmpty()) {
			defaultModel = availableModels.get(0);
		}

	}

	public List<String> synonyms(String word) {
		return List.of(word);
	}

	public List<LanguageModel> getModels() {
		return availableModels;
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

	public Optional<EmbeddingInfo> embed(String string, EmbeddingModel model) {
		return embedders.stream().filter(e -> e.accepts(model)).findFirst().map(e -> e.embed(string, model));
	}

	public List<Double> vectorize(String string) {
		Optional<EmbeddingModel> searchEmbeddingModel = getSearchEmbeddingModel();
		if (searchEmbeddingModel.isEmpty()) return null;
		return embed(string, searchEmbeddingModel.get()).map(EmbeddingInfo::getVector).orElseThrow();
	}

	public String prompt(String prompt) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		prompt(prompt, out);
		return out.toString(StandardCharsets.UTF_8);
	}

	public void prompt(String prompt, LanguageModel model, OutputStream out) {
		for (LanguageModelHost host : hosts) {
			if (model.getProvider().equals(host.name())) {
				host.prompt(prompt, model, out);
			}
		}
	}

	public void prompt(String prompt, OutputStream out) {
		prompt(prompt, getDefaultModel(), out);
	}

	public LanguageModel getDefaultModel() {
		return defaultModel;
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

	public void suggestTags(String text, List<String> tags, ServletOutputStream out) {
		String prompt = """
				<assistant-spec>
				<role>You are a skilled librarian</role>
				<objective>
				- Look carefully at the provided text and a list of possible tags
				- Select the tags that are relevant to the text
				</objective>
				<rules>
				- The selected tags can only be some of the provided possible tags
				- Do not produce tags not in the list of possible tags
				</rules>
				<text>{text}</text>
				<possible-tags>
				{tags}
				</possible-tags>
				</assistant-spec>
				<final-instruction>
				Output only the relevant tags separated by semicolon and no other text
				</final-instruction>
				""";
		prompt = prompt.replace("{text}", text).replace("{tags}", Strings.join(tags, "\n- "));
		prompt(prompt, out);
	}

	public static class EmbeddingsResponse {
		public List<List<Double>> embeddings;
	}

	public static class StreamResponse {
		public String response;
	}

	public Optional<LanguageModel> getModelById(String id) {
		for (LanguageModel model : models) {
			if (id.equals(model.getId())) {
				return Optional.of(model);
			}
		}
		return Optional.empty();
	}

	public Optional<EmbeddingModel> getSearchEmbeddingModel() {
		return configuration.getSearchEmbeddingModel();
	}

	// Wiring

	@Autowired
	public void setConfiguration(ConfigurationService configuration) {
		this.configuration = configuration;
	}

	@Autowired
	public void setHosts(List<LanguageModelHost> hosts) {
		this.hosts = hosts;
	}

	@Autowired
	public void setEmbedders(List<Embedder> embedders) {
		this.embedders = embedders;
	}
}
