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
import org.onlineobjects.modules.intelligence.Intelligence.EmbeddingsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.annotation.ApplicationScope;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.services.ConfigurationService;

@ApplicationScope
public class OllamaEmbedder implements Embedder {

	private static Logger log = LogManager.getLogger(OllamaEmbedder.class);

	private ConfigurationService configuration;

	@Override
	public boolean accepts(EmbeddingModel model) {
		switch (model) {
			case OLLAMA_GEMINI : {
				return true;
			}
			default :
				return false;
		}
	}

	@Override
	public EmbeddingInfo embed(String text, EmbeddingModel model) {
		Object payload = Map.of("model", "embeddinggemma", "input", text);
		try (var client = HttpClients.createDefault()) {
			ClassicHttpRequest request = ClassicRequestBuilder.post(getUrl(model))
					.setEntity(new StringEntity(
							Strings.toJSON(payload),
						    ContentType.APPLICATION_JSON))
		            .build();
			return client.execute(request, response -> {
				int code = response.getCode();
				String body = EntityUtils.toString(response.getEntity());
				if (code != 200) {
					throw new IOException(body);
				}
            	var parsed = Strings.fromJson(body, EmbeddingsResponse.class);
            	if (parsed.isPresent()) {
            		var r = parsed.get();
                	if (r.embeddings != null) {
	                    return EmbeddingInfo.create(r.embeddings.getFirst(), model);
					}
            	}
        		return null;
			});

		} catch (IOException e) {
			log.error(e);
		}
		return null;
	}

	private String getUrl(EmbeddingModel model) {
		return configuration.getOllamaUrl() + "/api/embed";
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
