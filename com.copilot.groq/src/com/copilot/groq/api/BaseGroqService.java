package com.copilot.groq.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.copilot.groq.Activator;
import com.copilot.groq.preferences.PreferenceConstants;

public abstract class BaseGroqService {
    protected static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    protected static final int MAX_RETRIES = 3;
    protected static final long RETRY_DELAY_MS = 1000; // Initial delay of 1 second
    protected static final int RATE_LIMIT_STATUS = 429;

    protected final HttpClient client;
    private final AtomicInteger requestCount;
    private volatile long lastRequestTime;
    private static final long MIN_REQUEST_INTERVAL_MS = 100; // Minimum time between requests

    protected BaseGroqService() {
        this.client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
        this.requestCount = new AtomicInteger(0);
        this.lastRequestTime = 0;
    }

    protected String getGroqApiKey() {
        // Try getting from configuration scope first
        IEclipsePreferences configPrefs = ConfigurationScope.INSTANCE.getNode(Activator.PLUGIN_ID);
        String apiKey = configPrefs.get(PreferenceConstants.P_API_KEY, null);

        // Fall back to instance scope if not found
        if (apiKey == null || apiKey.trim().isEmpty()) {
            IEclipsePreferences instancePrefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
            apiKey = instancePrefs.get(PreferenceConstants.P_API_KEY, null);

            // If found in instance scope, migrate it to configuration scope
            if (apiKey != null && !apiKey.trim().isEmpty()) {
                try {
                    configPrefs.put(PreferenceConstants.P_API_KEY, apiKey);
                    configPrefs.flush();
                } catch (Exception e) {
                    // Log the error but continue with the instance scope value
                    e.printStackTrace();
                }
            }
        }

        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new GroqApiException("Groq API key not configured. Please set it in preferences.");
        }

        return apiKey;
    }

    protected CompletableFuture<String> makeGroqApiCall(JSONObject requestBody) {
        return makeGroqApiCallWithRetry(requestBody, 0);
    }

    private CompletableFuture<String> makeGroqApiCallWithRetry(JSONObject requestBody, int retryCount) {
        try {
            // Rate limiting: Ensure minimum time between requests
            long currentTime = System.currentTimeMillis();
            long timeSinceLastRequest = currentTime - lastRequestTime;
            if (timeSinceLastRequest < MIN_REQUEST_INTERVAL_MS) {
                Thread.sleep(MIN_REQUEST_INTERVAL_MS - timeSinceLastRequest);
            }

            String apiKey = getGroqApiKey();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GROQ_API_URL))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

            lastRequestTime = System.currentTimeMillis();
            requestCount.incrementAndGet();

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenCompose(response -> {
                    try {
                        String responseBody = response.body();
                        if (response.statusCode() == RATE_LIMIT_STATUS) {
                            if (retryCount < MAX_RETRIES) {
                                long delayMs = calculateRetryDelay(retryCount);
                                CompletableFuture<String> retryFuture = new CompletableFuture<>();
                                scheduleRetry(requestBody, retryCount, delayMs, retryFuture);
                                return retryFuture;
                            } else {
                                throw new GroqApiException("Rate limit exceeded. Maximum retries attempted.");
                            }
                        }

                        if (responseBody == null || responseBody.trim().isEmpty()) {
                            throw new GroqApiException("Empty response from API");
                        }

                        JSONObject responseJson = new JSONObject(responseBody);

                        // Check for API errors
                        if (response.statusCode() != 200) {
                            String errorMessage = "API request failed with status code: " + response.statusCode();
                            if (responseJson.has("error")) {
                                JSONObject error = responseJson.optJSONObject("error");
                                if (error != null) {
                                    errorMessage = error.optString("message", errorMessage);
                                }
                            }
                            throw new GroqApiException(errorMessage);
                        }

                        // Extract content from response
                        JSONArray choices = responseJson.optJSONArray("choices");
                        if (choices == null || choices.length() == 0) {
                            throw new GroqApiException("No choices returned from API");
                        }

                        JSONObject choice = choices.optJSONObject(0);
                        if (choice == null) {
                            throw new GroqApiException("Invalid choice format in API response");
                        }

                        JSONObject message = choice.optJSONObject("message");
                        if (message == null) {
                            throw new GroqApiException("No message in API response");
                        }

                        String content = message.optString("content");
                        if (content == null || content.trim().isEmpty()) {
                            throw new GroqApiException("Empty content in API response");
                        }

                        return CompletableFuture.completedFuture(content);
                    } catch (JSONException e) {
                        throw new GroqApiException("Failed to parse API response: " + e.getMessage(), e);
                    }
                });
        } catch (Exception e) {
            CompletableFuture<String> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    private void scheduleRetry(JSONObject requestBody, int retryCount, long delayMs, CompletableFuture<String> future) {
        CompletableFuture.delayedExecutor(delayMs, TimeUnit.MILLISECONDS)
            .execute(() -> {
                try {
                    makeGroqApiCallWithRetry(requestBody, retryCount + 1)
                        .thenAccept(future::complete)
                        .exceptionally(e -> {
                            future.completeExceptionally(e);
                            return null;
                        });
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            });
    }

    private long calculateRetryDelay(int retryCount) {
        // Exponential backoff with jitter
        long baseDelay = RETRY_DELAY_MS * (1L << retryCount);
        long jitter = (long) (baseDelay * 0.1 * Math.random());
        return Math.min(baseDelay + jitter, 60000); // Cap at 60 seconds
    }

    protected JSONObject createRequestPayload(String systemPrompt, String userPrompt) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("model", "mixtral-8x7b-32768");

            JSONArray messagesArray = new JSONArray();

            // Add system message if provided
            if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
                JSONObject systemMessage = new JSONObject();
                systemMessage.put("role", "system");
                systemMessage.put("content", systemPrompt);
                messagesArray.put(systemMessage);
            }

            // Add user message
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", userPrompt != null ? userPrompt : "");
            messagesArray.put(userMessage);

            payload.put("messages", messagesArray);
            payload.put("temperature", 0.2);

            return payload;
        } catch (JSONException e) {
            throw new GroqApiException("Failed to create request payload: " + e.getMessage(), e);
        }
    }
}
