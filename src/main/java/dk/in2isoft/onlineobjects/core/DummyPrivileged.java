package dk.in2isoft.onlineobjects.core;

public class DummyPrivileged implements Privileged {

	private long identity;

	public DummyPrivileged(long identity) {
		super();
		this.identity = identity;
	}

	@Override
	public long getIdentity() {
		return identity;
	}

}
