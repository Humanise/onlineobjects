package dk.in2isoft.onlineobjects.core;

public interface Operator extends Privileged {

	public Operation getOperation();

	public void commit();

	public void rollBack();
	
	public Operator as(Privileged privileged);
}
