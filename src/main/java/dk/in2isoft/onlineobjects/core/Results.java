package dk.in2isoft.onlineobjects.core;

import org.hibernate.ScrollableResults;

public class Results<T> implements AutoCloseable {

	private ScrollableResults<T> results;

	protected Results(ScrollableResults<T> results) {
		this.results = results;
	}

	public boolean next() {
		return results.next();
	}

	public T get() {
		T object = results.get();
		return ModelService.getSubject(object);
	}

	public void close() {
		results.close();
	}
}
