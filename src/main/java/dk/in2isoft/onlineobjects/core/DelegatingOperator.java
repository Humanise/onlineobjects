package dk.in2isoft.onlineobjects.core;

public class DelegatingOperator implements Operator {

	private long identity;
	private Operator original;
	

	public DelegatingOperator(Operator original, Privileged privileged) {
		this.identity = privileged.getIdentity();
		this.original = original;
	}
	
	@Override
	public long getIdentity() {
		// TODO Auto-generated method stub
		return identity;
	}

	@Override
	public Operation getOperation() {
		// TODO Auto-generated method stub
		return original.getOperation();
	}

	@Override
	public void commit() {
		this.original.commit();
	}

	@Override
	public void rollBack() {
		this.original.rollBack();
	}

	@Override
	public Operator as(Privileged privileged) {
		if (privileged.getIdentity() == this.identity) return this;
		return new DelegatingOperator(this, privileged);
	}

}
