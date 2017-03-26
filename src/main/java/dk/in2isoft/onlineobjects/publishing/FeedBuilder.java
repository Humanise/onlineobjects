package dk.in2isoft.onlineobjects.publishing;

import dk.in2isoft.onlineobjects.core.Privileged;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;

public interface FeedBuilder {

	void buildFeed(Document document, FeedWriter writer, Privileged privileged) throws EndUserException;

}
