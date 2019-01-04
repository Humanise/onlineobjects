package dk.in2isoft.onlineobjects.apps.knowledge.views;

import java.util.List;
import java.util.Map.Entry;

import org.springframework.beans.factory.InitializingBean;

import com.google.common.collect.Lists;

import dk.in2isoft.commons.jsf.LegacyAbstractView;
import dk.in2isoft.onlineobjects.apps.knowledge.perspective.InternetAddressViewPerspectiveBuilder;
import dk.in2isoft.onlineobjects.core.Ability;
import dk.in2isoft.onlineobjects.modules.information.ContentExtractor;
import dk.in2isoft.onlineobjects.ui.jsf.model.Option;

public class KnowledgeView extends LegacyAbstractView implements InitializingBean {
	
	private InternetAddressViewPerspectiveBuilder builder;
	
	private String extractionAlgorithm;

	private List<Option> extractionOptions;
	
	private boolean debug;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		extractionOptions = Lists.newArrayList();
		for (Entry<String, ContentExtractor> ex : builder.getContentExtractors().entrySet()) {
			String value = ex.getKey();
			if (extractionAlgorithm==null) {
				extractionAlgorithm = value;
			}
			extractionOptions.add(new Option(value,value));
		}
		debug = getRequest().getSession().has(Ability.viewDebuggingInfo);
	}
	
	public String getExtractionAlgorithm() {
		return extractionAlgorithm;
	}

	public List<Option> getExtractionAlgorithms() {
		return extractionOptions;
	}
	
	public void setBuilder(InternetAddressViewPerspectiveBuilder builder) {
		this.builder = builder;
	}
	
	public boolean isDebug() {
		return debug;
	}
}
