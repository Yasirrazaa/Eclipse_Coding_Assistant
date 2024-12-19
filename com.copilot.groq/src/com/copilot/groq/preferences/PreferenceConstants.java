package com.copilot.groq.preferences;

/**
 * Constant definitions for plug-in preferences
 */
public class PreferenceConstants {

    // Cache settings
    public static final String P_CACHE_DURATION = "cacheDuration";

    // Rate limit settings
    public static final String P_RATE_LIMIT_PAUSE = "rateLimitPause";
    public static final String P_ENABLE_FALLBACK = "enableFallback";

    // API settings
    public static final String P_API_KEY = "apiKey";

    // UI settings
    public static final String P_INLINE_SUGGESTIONS = "inlineSuggestions";
    public static final String P_DARK_THEME = "darkTheme";

    // Default values
    public static final int DEFAULT_CACHE_DURATION = 30;
    public static final int DEFAULT_RATE_LIMIT_PAUSE = 60;
    public static final boolean DEFAULT_ENABLE_FALLBACK = true;
    public static final boolean DEFAULT_INLINE_SUGGESTIONS = true;
    public static final boolean DEFAULT_DARK_THEME = true;
}
