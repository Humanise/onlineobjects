package dk.in2isoft.onlineobjects.apps.words;

import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.RateLimiter;

import dk.in2isoft.onlineobjects.core.exceptions.TooBusyException;
import dk.in2isoft.onlineobjects.ui.Request;

public class LoadManager {
	private RateLimiter unknownLimiter = RateLimiter.create(5);
	private RateLimiter humanLimiter = RateLimiter.create(10);
	private RateLimiter botLimiter = RateLimiter.create(1);
	// private static EventCountCircuitBreaker breaker = new
	// EventCountCircuitBreaker(60, 1, TimeUnit.MINUTES, 40);
	private long timeout = 5;

	public void failIfBusy(Request request) throws TooBusyException {
		RateLimiter limiter = getLimiter(request);
		/*
		request.getResponse().addHeader("X-OO-Classification", request.getClient().getType().name());
		request.getResponse().addHeader("X-OO-Classification-UA",request.getUserAgent().orElse(""));
		*/
		if (!limiter.tryAcquire(timeout, TimeUnit.SECONDS)) {
			throw new TooBusyException("We are currently too busy, try again later");
		}
	}

	private RateLimiter getLimiter(Request request) {
		if (request.isHuman()) {
			return humanLimiter;
		}
		return request.isBot() ? botLimiter : unknownLimiter;
	}

	public double getRequestsPerSecond() {
		return unknownLimiter.getRate();
	}

	public void setRequestsPerSecond(double rate) {
		unknownLimiter.setRate(rate);
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}
}
