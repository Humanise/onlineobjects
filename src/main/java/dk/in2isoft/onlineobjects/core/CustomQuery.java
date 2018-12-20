package dk.in2isoft.onlineobjects.core;

import org.hibernate.query.NativeQuery;


public interface CustomQuery<T> {

	public String getSQL();
	
	public String getCountSQL();

	public T convert(Object[] row);

	public void setParameters(NativeQuery<?> sql);
}
