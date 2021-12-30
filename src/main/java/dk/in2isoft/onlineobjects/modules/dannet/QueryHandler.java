package dk.in2isoft.onlineobjects.modules.dannet;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.util.iterator.ExtendedIterator;

public class QueryHandler {
	
	private Graph graph;

	public QueryHandler(Graph graph) {
		super();
		this.graph = graph;
	}

	public ExtendedIterator<Node> objectsFor(Node subject, Node predicate) {
		return graph.find(subject, predicate, null).mapWith(t -> t.getObject());
	}

	public ExtendedIterator<Node> subjectsFor(Node predicate, Node object) {
		return graph.find(null, predicate, object).mapWith(t -> t.getSubject());
	}

}
