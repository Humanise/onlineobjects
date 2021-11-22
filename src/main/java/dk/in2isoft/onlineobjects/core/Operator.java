package dk.in2isoft.onlineobjects.core;

public interface Operator extends Privileged, AutoCloseable {

	public Operation getOperation();

	public void commit();

	public void rollBack();
	
	public Operator as(Privileged privileged);

	public void close();
}
