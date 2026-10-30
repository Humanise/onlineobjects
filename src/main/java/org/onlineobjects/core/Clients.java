package org.onlineobjects.core;

import java.util.Set;

import org.onlineobjects.common.BoundedFifoSet;
import org.onlineobjects.core.Client.Type;

import dk.in2isoft.onlineobjects.ui.Request;

public class Clients {

	private BoundedFifoSet<String> humanIps = new BoundedFifoSet<String>(10000);
	private BoundedFifoSet<String> botAgents = new BoundedFifoSet<String>(10000);

	public void classify(Request request) {
		String ip = request.getRequest().getRemoteAddr();
		Client client = request.getClient();
		if (humanIps.contains(ip)) {
			client.setType(Type.HUMAN);
		}
		request.getUserAgent().map(String::toLowerCase).ifPresent(ua -> {
			if (ua.contains("facebook") || ua.contains("bot")) {
				client.setType(Type.BOT);
				botAgents.add(ua);
			}
		});
		if (client.getType().equals(Type.UNDETERMINED)) {
			//client.setType(Type.UNKNOWN);
		}
	}

	public void markHuman(Request request) {
		request.getClient().setType(Type.HUMAN);
		humanIps.add(request.getRequest().getRemoteAddr());
	}

	public Set<String> getHumanIps() {
		return humanIps.snapshot();
	}

	public Set<String> getBotAgents() {
		return botAgents.snapshot();
	}
}
