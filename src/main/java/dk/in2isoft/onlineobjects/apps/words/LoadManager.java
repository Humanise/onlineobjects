package dk.in2isoft.onlineobjects.apps.words;

import java.time.Duration;

import com.google.common.util.concurrent.RateLimiter;

import dk.in2isoft.onlineobjects.core.exceptions.TooBusyException;

public class LoadManager {
	private static RateLimiter limiter = RateLimiter.create(1);
	//private static EventCountCircuitBreaker breaker = new EventCountCircuitBreaker(60, 1, TimeUnit.MINUTES, 40);
	
	public void failIfBusy() throws TooBusyException {
		if (!limiter.tryAcquire(Duration.ofSeconds(5))) {
			throw new TooBusyException("We are currently too busy, try again later");
		}

	}
}
