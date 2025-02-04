package org.onlineobjects.modules.intelligence;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.springframework.web.context.annotation.ApplicationScope;

import dk.in2isoft.commons.lang.Strings;

@ApplicationScope
public class Intelligence {

	public List<String> synonyms(String word) {
		return List.of(word);
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

	public List<Double> vectorize(String string) {
		Object payload = Map.of("model", "nomic-embed-text", "input", string);
		Optional<EmbeddingsResponse> response = fetch("embed", payload, EmbeddingsResponse.class);
		return response.map(r -> r.embeddings.get(0)).orElse(null);
	}

	public String prompt(String prompt) {
		Object payload = Map.of("model", "llama3.2", "prompt", prompt);
		Optional<String> response = fetch("generate", payload, String.class);
		return response.orElse(null);
	}
	
	public static class EmbeddingsResponse {
		public List<List<Double>> embeddings;
	}
}
