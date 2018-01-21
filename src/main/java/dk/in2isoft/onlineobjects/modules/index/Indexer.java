package dk.in2isoft.onlineobjects.modules.index;

import java.util.List;

public interface Indexer {

	List<IndexDescription> getIndexInstances();
	
	boolean is(IndexDescription description);
	
	long getObjectCount(IndexDescription description);
}
