package org.onlineobjects.modules.index;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpJdkSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.springframework.web.context.annotation.ApplicationScope;

@ApplicationScope
public class SolrService {

	public enum Collection {knowledge}

	private HttpJdkSolrClient client;

	public SolrClient getClient() {
		if (client != null) return this.client;
		final String solrUrl = "http://localhost:8983/solr";
		this.client = new HttpJdkSolrClient.Builder(solrUrl)
			.withConnectionTimeout(10000, TimeUnit.MILLISECONDS)
			.withBasicAuthCredentials("humanise", "Kuw8LJz7kDnznyRJYckH8G3csYzdHH")
			.build();
		return this.client;
	}

	public void add(Collection collection, SolrInputDocument doc) throws SolrServerException, IOException {
		getClient().add(collection.name(), doc);
	}

	public void add(Collection collection, Document document) throws SolrServerException, IOException {
		SolrInputDocument doc = new SolrInputDocument();
		document.getFields().forEach(field -> {
			doc.addField(field.name(), getValue(field));
		});
		add(collection, doc);
	}

	private Object getValue(IndexableField field) {
		return field.stringValue();
	}

	public QueryResponse query(Collection collection, Map<String, String> map) {
		SolrClient client = getClient();
		SolrParams params = new MapSolrParams(map);
		try {
			return client.query(collection.name(), params, METHOD.POST);
		} catch (SolrServerException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
