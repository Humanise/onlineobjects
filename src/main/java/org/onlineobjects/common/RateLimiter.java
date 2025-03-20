package org.onlineobjects.common;

public class RateLimiter {
	private final long maxTokens;
	private final long refillTokensPerSecond;
	private double availableTokens;
	private long lastRefillTimestamp;

	public RateLimiter(long maxTokens, long refillTokensPerSecond) {
		this.maxTokens = maxTokens;
		this.refillTokensPerSecond = refillTokensPerSecond;
		this.availableTokens = maxTokens;
		this.lastRefillTimestamp = System.currentTimeMillis();
	}

	public synchronized boolean tryAcquire() {
		refill();
		if (availableTokens >= 1) {
			availableTokens -= 1;
			return true;
		}
		return false;
	}

	private void refill() {
		long now = System.currentTimeMillis();
		double tokensToAdd = (now - lastRefillTimestamp) / 1000.0 * refillTokensPerSecond;
		availableTokens = Math.min(availableTokens + tokensToAdd, maxTokens);
		lastRefillTimestamp = now;
	}
}