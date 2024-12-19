package com.copilot.groq.util;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Manages rate limiting and tracks API call status
 */
public class RateLimitManager {
    private static RateLimitManager instance;
    private final ConcurrentHashMap<String, AtomicLong> lastCallTime;
    private final AtomicBoolean isRateLimited;
    private Instant rateLimitResetTime;

    private RateLimitManager() {
        this.lastCallTime = new ConcurrentHashMap<>();
        this.isRateLimited = new AtomicBoolean(false);
    }

    public static synchronized RateLimitManager getInstance() {
        if (instance == null) {
            instance = new RateLimitManager();
        }
        return instance;
    }

    /**
     * Check if an API call can be made
     * @param operationType Type of operation (e.g., "completion", "analysis")
     * @return true if call is allowed, false if rate limited
     */
    public boolean canMakeCall(String operationType) {
        if (isRateLimited.get()) {
            if (Instant.now().isAfter(rateLimitResetTime)) {
                isRateLimited.set(false);
            } else {
                return false;
            }
        }

        AtomicLong last = lastCallTime.computeIfAbsent(operationType, k -> new AtomicLong(0));
        long currentTime = System.currentTimeMillis();
        long timeSinceLastCall = currentTime - last.get();

        // Allow call if enough time has passed
        return timeSinceLastCall >= 1000; // Minimum 1 second between calls
    }

    /**
     * Record a successful API call
     * @param operationType Type of operation
     */
    public void recordSuccessfulCall(String operationType) {
        lastCallTime.computeIfAbsent(operationType, k -> new AtomicLong())
                   .set(System.currentTimeMillis());
    }

    /**
     * Handle rate limit error
     */
    public void handleRateLimit() {
        isRateLimited.set(true);
        // Default pause of 1 minute if no specific pause is set
        int pauseDuration = 1;
        rateLimitResetTime = Instant.now().plusSeconds(pauseDuration * 60L);
    }

    /**
     * Check if currently rate limited
     */
    public boolean isRateLimited() {
        if (isRateLimited.get() && Instant.now().isAfter(rateLimitResetTime)) {
            isRateLimited.set(false);
        }
        return isRateLimited.get();
    }

    /**
     * Get time until rate limit reset
     * @return Seconds until reset, or 0 if not rate limited
     */
    public long getSecondsUntilReset() {
        if (!isRateLimited.get()) {
            return 0;
        }
        return Instant.now().until(rateLimitResetTime, java.time.temporal.ChronoUnit.SECONDS);
    }

    /**
     * Reset rate limit status
     */
    public void reset() {
        isRateLimited.set(false);
        lastCallTime.clear();
    }
}
