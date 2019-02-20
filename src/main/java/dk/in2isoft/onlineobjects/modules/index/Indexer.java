package dk.in2isoft.onlineobjects.modules.index;

import java.util.List;

import dk.in2isoft.onlineobjects.core.Operator;

public interface Indexer {

	List<IndexDescription> getIndexInstances(Operator operator);
	
	boolean is(IndexDescription description);
	
	long getObjectCount(IndexDescription description, Operator operator);
}
