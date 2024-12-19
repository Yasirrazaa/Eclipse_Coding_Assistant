package com.copilot.groq.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.json.JSONArray;
import org.json.JSONObject;

import com.copilot.groq.Activator;

public class GroqApiService {
    private static final String GROQ_API_URL = "https://api.groq.com/v1/completions";
    private final HttpClient client;

    public GroqApiService() {
        this.client = HttpClient.newHttpClient();
    }

    public CompletableFuture<List<String>> getCodeSuggestions(String context, String prefix) {
        String apiKey = getGroqApiKey();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return CompletableFuture.failedFuture(
                new IllegalStateException("Groq API key not configured. Please set it in preferences."));
        }

        JSONObject message = new JSONObject()
            .put("role", "system")
            .put("content", "You are an AI coding assistant. Provide code completion suggestions for Java code.");

        JSONObject userMessage = new JSONObject()
            .put("role", "user")
            .put("content", String.format("Given this code context:\n%s\n\nProvide completion suggestions for: %s",
                context, prefix));

        JSONArray messages = new JSONArray()
            .put(message)
            .put(userMessage);

        JSONObject requestBody = new JSONObject()
            .put("model", "mixtral-8x7b-32768")
            .put("messages", messages)
            .put("temperature", 0.3)
            .put("max_tokens", 150);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(GROQ_API_URL))
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
            .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                if (response.statusCode() != 200) {
                    throw new RuntimeException("API request failed: " + response.body());
                }
                return parseSuggestionsFromResponse(response.body());
            });
    }

    private List<String> parseSuggestionsFromResponse(String responseBody) {
        List<String> suggestions = new ArrayList<>();
        JSONObject response = new JSONObject(responseBody);

        JSONArray choices = response.getJSONArray("choices");
        for (int i = 0; i < choices.length(); i++) {
            JSONObject choice = choices.getJSONObject(i);
            JSONObject message = choice.getJSONObject("message");
            String content = message.getString("content");

            // Parse the content to extract code suggestions
            // This is a simple implementation - you might want to enhance this
            for (String line : content.split("\n")) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("```") && !line.startsWith("#")) {
                    suggestions.add(line);
                }
            }
        }

        return suggestions;
    }

    private String getGroqApiKey() {
        try {
            IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
            String apiKey = prefs.get("groq_api_key", null);

            if (apiKey == null || apiKey.trim().isEmpty()) {
                // Optional: Log a warning or throw a custom exception
                System.err.println("Groq API Key not found in preferences");
                return null;
            }

            return apiKey.trim();
        } catch (Exception e) {
            // Log the exception for debugging
            System.err.println("Error retrieving Groq API Key: " + e.getMessage());
            return null;
        }
    }
}
