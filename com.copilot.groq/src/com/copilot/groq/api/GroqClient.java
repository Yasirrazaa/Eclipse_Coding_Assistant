package com.copilot.groq.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.Preferences;

public class GroqClient {
    private final HttpClient client;
    private static final String GROQ_API_BASE_URL = "https://api.groq.com/v1/completions";
    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    private static final String PREFERENCES_NODE = "com.example.myplugin";
    private static final String API_KEY_PREF = "groq_api_key";

    public GroqClient() {
        this.client = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .build();
    }

    private String getApiKey() {
        Preferences prefs = InstanceScope.INSTANCE.getNode(PREFERENCES_NODE);
        String apiKey = prefs.get(API_KEY_PREF, "");
        if (apiKey.isEmpty()) {
            throw new GroqApiException("Groq API key not found. Please set it in the preferences.");
        }
        return apiKey;
    }

    public CompletableFuture<String> sendRequest(String endpoint, String requestBody) {
        String apiKey;
        try {
            apiKey = getApiKey();
        } catch (GroqApiException e) {
            return CompletableFuture.failedFuture(e);
        }

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(GROQ_API_BASE_URL + endpoint))
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .timeout(TIMEOUT)
            .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(HttpResponse::body)
            .exceptionally(ex -> {
                // Log the error or handle it
                return "Error: " + ex.getMessage();
            });
    }
}
