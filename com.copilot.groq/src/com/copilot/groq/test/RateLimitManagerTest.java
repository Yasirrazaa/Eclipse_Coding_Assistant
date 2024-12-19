package com.copilot.groq.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import com.copilot.groq.util.RateLimitManager;

public class RateLimitManagerTest {
    private RateLimitManager manager;

    @Before
    public void setUp() {
        manager = RateLimitManager.getInstance();
        manager.reset();
    }

    @Test
    public void testInitialState() {
        assertFalse("Should not be rate limited initially", manager.isRateLimited());
        assertEquals("Reset time should be 0", 0, manager.getSecondsUntilReset());
    }

    @Test
    public void testRateLimit() {
        assertTrue("Should be able to make first call", manager.canMakeCall("test"));
        manager.recordSuccessfulCall("test");

        // Test rate limiting
        manager.handleRateLimit();
        assertTrue("Should be rate limited", manager.isRateLimited());
        assertTrue("Should have positive reset time", manager.getSecondsUntilReset() > 0);
        assertFalse("Should not allow calls while rate limited", manager.canMakeCall("test"));
    }

    @Test
    public void testMultipleOperations() {
        assertTrue("Should allow first call", manager.canMakeCall("op1"));
        manager.recordSuccessfulCall("op1");

        assertTrue("Should allow different operation", manager.canMakeCall("op2"));
        manager.recordSuccessfulCall("op2");

        // Wait 1 second
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            fail("Test interrupted");
        }

        assertTrue("Should allow call after delay", manager.canMakeCall("op1"));
    }

    @Test
    public void testReset() {
        manager.handleRateLimit();
        assertTrue(manager.isRateLimited());

        manager.reset();
        assertFalse("Should not be rate limited after reset", manager.isRateLimited());
        assertTrue("Should allow calls after reset", manager.canMakeCall("test"));
    }
}
