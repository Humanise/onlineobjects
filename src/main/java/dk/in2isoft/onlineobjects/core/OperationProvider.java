package dk.in2isoft.onlineobjects.core;

public interface OperationProvider {

	public Operation newOperation();

	public void execute(Operation operation);

	public void rollBack(Operation operation);
}
