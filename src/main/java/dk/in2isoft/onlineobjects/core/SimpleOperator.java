package dk.in2isoft.onlineobjects.core;

public class SimpleOperator implements Operator {

	private long identity;
	private OperationProvider operationProvider;
	private Operation operation;
	
	public SimpleOperator(long identity, OperationProvider operationProvider) {
		super();
		this.identity = identity;
		this.operationProvider = operationProvider;
	}

	@Override
	public long getIdentity() {
		return identity;
	}

	@Override
	public Operation getOperation() {
		if (operation == null) {
			operation = operationProvider.newOperation();
		}
		return operation;
	}

	@Override
	public void commit() {
		if (operation != null) {
			operationProvider.execute(operation);
			operation = null;
		}

	}

	@Override
	public void rollBack() {
		if (operation != null) {
			operationProvider.rollBack(operation);
			operation = null;
		}
	}

}
