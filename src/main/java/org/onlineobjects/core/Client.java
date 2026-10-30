package org.onlineobjects.core;

public class Client {

	public enum Type {
		HUMAN, BOT, UNDETERMINED, UNKNOWN
	}

	private Type type = Type.UNDETERMINED;

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public boolean isHuman() {
		return Type.HUMAN.equals(type);
	}

	public boolean isBot() {
		return Type.BOT.equals(type);
	}

	public static Client ofType(Type type) {
		Client client = new Client();
		client.type = Type.UNDETERMINED;
		return client;
	}

	public static Client newUndetermined() {
		return ofType(Client.Type.UNDETERMINED);
	}

}
