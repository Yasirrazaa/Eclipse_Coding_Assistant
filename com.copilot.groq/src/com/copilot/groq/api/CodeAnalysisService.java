package com.copilot.groq.api;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.json.JSONObject;

public class CodeAnalysisService extends BaseGroqService {

    public CompletableFuture<List<String>> analyzeCode(String code) {
        JSONObject requestBody = createRequestPayload(
            "You are a Java code analyzer. Your task is to analyze the given code and provide insights. " +
            "Format your response as a bullet-point list, with each point on a new line starting with '- '.\n" +
            "Include analysis of:\n" +
            "1. Code quality\n" +
            "2. Potential bugs\n" +
            "3. Performance considerations\n" +
            "4. Best practices\n" +
            "5. Suggestions for improvement",
            code
        );

        return makeGroqApiCall(requestBody)
            .thenApply(response -> {
                try {
                    // Split the response into lines and filter for bullet points
                    return Arrays.stream(response.split("\n"))
                        .map(String::trim)
                        .filter(line -> line.startsWith("-"))
                        .map(line -> line.substring(1).trim())
                        .collect(Collectors.toList());
                } catch (Exception e) {
                    throw new GroqApiException("Failed to process analysis response: " + e.getMessage(), e);
                }
            })
            .exceptionally(ex -> {
                String errorMsg = "Error during code analysis: " + ex.getMessage();
                System.err.println(errorMsg);
                List<String> errorList = Arrays.asList(errorMsg);
                return errorList;
            });
    }
}
