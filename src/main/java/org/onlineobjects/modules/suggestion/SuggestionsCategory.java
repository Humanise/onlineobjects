package org.onlineobjects.modules.suggestion;

import java.util.Collections;
import java.util.List;

public class SuggestionsCategory {

	private String description;
	
	private List<Suggestion> suggestions = Collections.EMPTY_LIST;
	
	private boolean dirty;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Suggestion> getSuggestions() {
		return suggestions;
	}

	public void setSuggestions(List<Suggestion> suggestions) {
		this.suggestions = suggestions;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}
}
