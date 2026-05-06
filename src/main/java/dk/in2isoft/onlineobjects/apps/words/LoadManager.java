package dk.in2isoft.onlineobjects.apps.words;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.RateLimiter;

import dk.in2isoft.onlineobjects.core.exceptions.TooBusyException;
import dk.in2isoft.onlineobjects.ui.Request;

public class LoadManager {
	private RateLimiter limiter = RateLimiter.create(5);
	private RateLimiter botLimiter = RateLimiter.create(1);
	// private static EventCountCircuitBreaker breaker = new
	// EventCountCircuitBreaker(60, 1, TimeUnit.MINUTES, 40);
	private long timeout = 5;

	public void failIfBusy(Request request) throws TooBusyException {
		RateLimiter limiter = getLimiter(request);
		if (!limiter.tryAcquire(timeout, TimeUnit.SECONDS)) {
			throw new TooBusyException("We are currently too busy, try again later");
		}
	}

	private RateLimiter getLimiter(Request request) {
		Optional<String> agent = request.getUserAgent();
		return agent.map(ua -> {
			if (isBot(ua)) {
				return botLimiter;
			} else {
				return limiter;
			}
		}).orElse(limiter);
	}

	private boolean isBot(String userAgent) {
		return userAgent.contains("facebook") || userAgent.contains("bot");
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
