package org.onlineobjects.modules.intelligence;

import org.hibernate.query.NativeQuery;
import org.onlineobjects.modules.intelligence.EmbeddingQuery.EmbeddingResult;

import dk.in2isoft.onlineobjects.core.CustomQuery;
import dk.in2isoft.onlineobjects.core.Privileged;
import dk.in2isoft.onlineobjects.model.Entity;

public class EmbeddingQuery implements CustomQuery<EmbeddingResult> {

	public static class EmbeddingResult {
		long itemId;
		double similarity;

		public long getItemId() {
			return itemId;
		}

		public double getSimilarity() {
			return similarity;
		}
	}

	private long privileged;
	private long itemId;
	private EmbeddingModel model;
	private Class<? extends Entity> type;

	@Override
	public String getSQL() {
		return """
				SELECT
					entity_id,
					embedding <=> (SELECT embedding FROM embedding WHERE entity_id = :id AND model_id = :model)
				FROM embedding
				WHERE entity_id IN (
					SELECT other.id FROM :type as other
					JOIN privilege ON privilege.object = other.id
					AND privilege.subject = :privileged
					AND privilege.view = true
				)
				AND model_id = :model
				ORDER BY
					(embedding <=> (SELECT embedding FROM embedding WHERE entity_id = :id AND model_id = :model))
				LIMIT 20
				""".replace(":type", type.getSimpleName().toLowerCase());
	}

	@Override
	public String getCountSQL() {
		return "select 20";
	}

	@Override
	public EmbeddingResult convert(Object[] row) {
		EmbeddingResult result = new EmbeddingResult();
		result.itemId = (long) row[0];
		if (row[1] != null) {
			result.similarity = 1 - ((double) row[1] / 2d);
		}
		//result.similarity = (double) row[1];
		return result;
	}

	@Override
	public void setParameters(NativeQuery<?> sql) {
		sql.setParameter("id", itemId);
		sql.setParameter("privileged", privileged);
		sql.setParameter("model", model.getId());
		//sql.setParameter("type", type.getSimpleName().toLowerCase());

	}

	public static EmbeddingQuery create(EmbeddingModel model, Entity entity, Class<? extends Entity> type, Privileged privleged) {
		EmbeddingQuery query = new EmbeddingQuery();
		query.model = model;
		query.privileged = privleged.getIdentity();
		query.itemId = entity.getId();
		query.type = type;
		return query;
	}
}
