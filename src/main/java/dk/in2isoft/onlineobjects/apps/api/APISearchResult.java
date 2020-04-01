package dk.in2isoft.onlineobjects.apps.api;

import java.util.List;

import dk.in2isoft.onlineobjects.core.SearchResult;
import dk.in2isoft.onlineobjects.modules.knowledge.ApiPerspective;

public class APISearchResult extends SearchResult<KnowledgeListRow> implements ApiPerspective {
	
	private long version;

	public APISearchResult(List<KnowledgeListRow> result, int totalCount) {
		super(result, totalCount);
		version = System.currentTimeMillis();
	}

	public APISearchResult(SearchResult<KnowledgeListRow> result) {
		this(result.getList(), result.getTotalCount());
	}

	@Override
	public long getVersion() {
		return version;
	}
	
	public void setVersion(long version) {
		this.version = version;
	}
}
