package dk.in2isoft.onlineobjects.apps.knowledge.index;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.onlineobjects.modules.index.SolrService;
import org.onlineobjects.modules.index.SolrService.Collection;
import org.onlineobjects.modules.intelligence.Intelligence;
import org.springframework.beans.factory.annotation.Autowired;

import dk.in2isoft.onlineobjects.core.Privileged;

public class KnowledgeSolrIndexReader {

	private SolrService solr;
	private Intelligence intelligence;

	public List<KnowledgeIndexDocument> findSimilar(String text, Privileged user) {

		List<Double> vector = intelligence.vectorize(text);

		var query = new HashMap<String, String>();

		String q = buildQ(vector);

		query.put("q", q);
		query.put("fq", "owner_id:" + user.getIdentity());

		QueryResponse response = solr.query(Collection.knowledge, query);

		return response.getResults().stream().map(result -> {
			var doc = new KnowledgeIndexDocument();
			doc.setTitle(String.valueOf(result.getFirstValue("title")));
			doc.setType(String.valueOf(result.getFirstValue("type")));
			doc.setId(Long.parseLong(String.valueOf(result.getFirstValue("id"))));
			return doc;
		}).collect(Collectors.toList());
	}

	private String buildQ(List<Double> vector) {
		StringBuilder q = new StringBuilder();
		if (true) {
			q.append("{!vectorSimilarity f=vector minReturn=0.7}[");
		} else {
			q.append("{!knn f=vector topK=10}[");
		}
		for (Iterator<Double> i = vector.iterator(); i.hasNext();) {
			Double v = i.next();
			q.append(v);
			if (i.hasNext()) {
				q.append(",");
			}
		}
		q.append("]");
		return q.toString();
	}

	@Autowired
	public void setSolrService(SolrService solr) {
		this.solr = solr;
	}

	@Autowired
	public void setIntelligence(Intelligence intelligence) {
		this.intelligence = intelligence;
	}
}
