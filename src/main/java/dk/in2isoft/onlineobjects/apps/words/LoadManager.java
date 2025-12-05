package dk.in2isoft.onlineobjects.apps.words;

import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.RateLimiter;

import dk.in2isoft.onlineobjects.core.exceptions.TooBusyException;

public class LoadManager {
	private RateLimiter limiter = RateLimiter.create(5);
	// private static EventCountCircuitBreaker breaker = new
	// EventCountCircuitBreaker(60, 1, TimeUnit.MINUTES, 40);
	private long timeout = 5;

	public void failIfBusy() throws TooBusyException {
		if (!limiter.tryAcquire(timeout, TimeUnit.SECONDS)) {
			throw new TooBusyException("We are currently too busy, try again later");
		}
	}

	public double getRequestsPerSecond() {
		return limiter.getRate();
	}

	public void setRequestsPerSecond(double rate) {
		limiter.setRate(rate);
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}
}
