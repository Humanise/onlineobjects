package dk.in2isoft.onlineobjects.apps.knowledge.index;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.apache.lucene.document.Document;
import org.apache.solr.common.SolrInputDocument;
import org.onlineobjects.modules.index.SolrService;
import org.onlineobjects.modules.index.SolrService.Collection;
import org.onlineobjects.modules.intelligence.Intelligence;
import org.springframework.beans.factory.annotation.Autowired;

import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.services.ConfigurationService;

public class KnowledgeSolrIndexWriter {

	private SolrService solr;
	private ConfigurationService configuration;
	private Intelligence intelligence;

	private static final Logger log = LogManager.getLogger(KnowledgeSolrIndexWriter.class);

	protected void index(Entity entity, User owner, Document document) throws EndUserException {
		if (configuration.isSolrEnabled()) {
			try {
				SolrInputDocument solrDoc = new SolrInputDocument();
				solrDoc.addField("id", entity.getId());
				solrDoc.addField("type", document.getField("type").stringValue());
				solrDoc.addField("owner_id", owner.getId());
				solrDoc.addField("title", document.getField("title").stringValue());
				String text = document.getField("text").stringValue();
				solrDoc.addField("text", text);
				String favorite = document.getField("favorite").stringValue();
				solrDoc.addField("favorite", "yes".equals(favorite) );
				String inbox = document.getField("inbox").stringValue();
				solrDoc.addField("inbox", "yes".equals(inbox));
				if (Strings.isNotBlank(text)) {
					List<Double> vector = intelligence.vectorize(text);
					solrDoc.addField("vector", vector);
				}
				solr.add(Collection.knowledge, solrDoc);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	@Autowired
	public void setSolrService(SolrService solr) {
		this.solr = solr;
	}

	@Autowired
	public void setConfigurationService(ConfigurationService configuration) {
		this.configuration = configuration;
	}

	@Autowired
	public void setIntelligence(Intelligence intelligence) {
		this.intelligence = intelligence;
	}
}
