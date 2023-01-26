package dk.in2isoft.onlineobjects.apps.knowledge.perspective;

import java.util.List;

import dk.in2isoft.onlineobjects.ui.data.Option;

public interface TaggableViewPerspective {

	public List<Option> getTags();
	
	public void setTags(List<Option> tags);
}
