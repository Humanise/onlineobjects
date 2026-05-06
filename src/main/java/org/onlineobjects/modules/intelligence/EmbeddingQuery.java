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

	@Override
	public String getSQL() {
		return """
				SELECT
					entity_id,
					embedding <=> (select embedding from embedding where entity_id = :id)
				FROM embedding
				WHERE entity_id IN (
					select question.id from question
					join privilege on privilege.object = question.id
					and privilege.subject = :privileged
					and privilege.view = true
				)
				ORDER BY
					(embedding <=> (select embedding from embedding where entity_id = :id))
				LIMIT 20
				""";
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

	}

	public static EmbeddingQuery create(Entity entity, Privileged privleged) {
		EmbeddingQuery query = new EmbeddingQuery();
		query.privileged = privleged.getIdentity();
		query.itemId = entity.getId();
		return query;
	}
}
