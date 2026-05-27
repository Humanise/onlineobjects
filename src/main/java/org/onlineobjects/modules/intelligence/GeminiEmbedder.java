package org.onlineobjects.modules.intelligence;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.services.ConfigurationService;

@ApplicationScope
public class GeminiEmbedder implements Embedder {

	private class TooManyRequests extends IOException {

		private static final long serialVersionUID = 1L;

		public TooManyRequests(String body) {
			super(body);
		}

	}

	private static Logger log = LogManager.getLogger(GeminiEmbedder.class);

	private ConfigurationService configuration;

	@Override
	public boolean accepts(EmbeddingModel model) {
		switch (model) {
			case GEMINI_1, GEMINI_1_SMALL, GEMINI_2_PREVIEW, GEMINI_2_PREVIEW_SMALL : {
				return true;
			}
			default :
				return false;
		}
	}

	@Override
	public EmbeddingInfo embed(String text, EmbeddingModel model) {
		Object payload = Map.of(
				"taskType", "SEMANTIC_SIMILARITY",
				"output_dimensionality", model.getDimensions(),
				"content", Map.of("parts", List.of(Map.of("text", text))));
		try {
			return request(model, payload);
		} catch (TooManyRequests e) {
			if (false) {
				log.warn("Embedding is exhausted, trying again in 60s");
				try {
					// Wait for 60 seconds
					Thread.sleep(1000 * 60);
				} catch (InterruptedException ignore) {}
				try {
					return request(model, payload);
				} catch (IOException e1) {
					log.error(e);
					return null;
				}
			} else {
				log.error(e);
				return null;
			}
		} catch (IOException e) {
			log.error(e);
			return null;
		}
	}

	private EmbeddingInfo request(EmbeddingModel model, Object payload) throws IOException {
		try (var client = HttpClients.createDefault()) {
			ClassicHttpRequest request = ClassicRequestBuilder.post(getUrl(model))
					.setEntity(new StringEntity(
							Strings.toJSON(payload),
						    ContentType.APPLICATION_JSON))
					.addHeader("x-goog-api-key", configuration.getGeminiApiKey())
		            .build();
			return client.execute(request, response -> {
				int code = response.getCode();
				String body = EntityUtils.toString(response.getEntity());
				if (code != 200) {
					if (code == 429) {
						// TODO: This should check if the quota is exceeded and retry later
						throw new TooManyRequests(body);
					} else {
						throw new IOException(body);
					}
				}
            	var parsed = Strings.fromJson(body, Response.class);
            	if (parsed.isPresent()) {
            		var r = parsed.get();
                	if (r.embedding != null) {
	                    return EmbeddingInfo.create(r.embedding.values, model);
					}
            	}
        		return null;
			});
		}
	}

	private String getUrl(EmbeddingModel model) {
		switch (model) {
			case GEMINI_1, GEMINI_1_SMALL :
				return "https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-001:embedContent";
			case GEMINI_2_PREVIEW, GEMINI_2_PREVIEW_SMALL :
				return "https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-2-preview:embedContent";
			case OLLAMA_GEMINI :
				throw new IllegalStateException();
		}
		throw new IllegalStateException();
	}

	@Autowired
	public void setConfiguration(ConfigurationService configuration) {
		this.configuration = configuration;
	}

	public record ResponseEmbedding(List<Double> values) {

	}

	public static class Response {
		public ResponseEmbedding embedding;
	}
}

/*
curl "https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-001:embedContent" \
 -H "Content-Type: application/json" \
 -H "x-goog-api-key: $GEMINI_API_KEY" \
 -d '{
 "content": {
     "parts": [
     {
         "text": "What is the meaning of life?"
     },
     {
         "text": "How much wood would a woodchuck chuck?"
     },
     {
         "text": "How does the brain work?"
     }
     ]
 },
 "taskType": "SEMANTIC_SIMILARITY"
 }'
*/