package com.copilot.groq.api;

import java.util.concurrent.CompletableFuture;

import org.json.JSONObject;

public class CodeGenerationService extends BaseGroqService {

    public CompletableFuture<String> generateCode(String prompt) {
        JSONObject requestBody = createRequestPayload(
            "You are a Java code generation expert. Your task is to generate high-quality Java code based on the given prompt. " +
            "Format your response as follows:\n" +
            "1. First, provide the generated code wrapped in ```java and ``` tags\n" +
            "2. Make sure to include all necessary imports\n" +
            "3. Add comprehensive comments explaining the code",
            prompt
        );

        return makeGroqApiCall(requestBody)
            .thenApply(response -> {
                try {
                    // Extract code from markdown code blocks
                    String code = response;
                    if (response.contains("```java")) {
                        code = response.substring(
                            response.indexOf("```java") + 7,
                            response.lastIndexOf("```")
                        ).trim();
                    } else if (response.contains("```")) {
                        code = response.substring(
                            response.indexOf("```") + 3,
                            response.lastIndexOf("```")
                        ).trim();
                    }

                    // If no code block found, use the entire response
                    return code.isEmpty() ? response.trim() : code;
                } catch (Exception e) {
                    // More detailed error handling
                    System.err.println("Error processing generation response: " + e.getMessage());
                    System.err.println("Received Response: " + response);
                    throw new GroqApiException("Failed to process generation response: " + e.getMessage(), e);
                }
            })
            .exceptionally(ex -> {
                String errorMsg = "Error during code generation: " + ex.getMessage();
                System.err.println(errorMsg);
                return errorMsg;
            });
    }

    public CompletableFuture<String> analyzeCode(String code) {
        JSONObject requestBody = createRequestPayload(
            "You are a Java code analysis expert. Your task is to analyze the given code and provide insights. " +
            "Focus on:\n" +
            "1. Code structure and organization\n" +
            "2. Potential improvements\n" +
            "3. Best practices compliance\n" +
            "4. Performance considerations\n" +
            "Format your response in a clear, structured way with sections and bullet points.",
            code
        );

        return makeGroqApiCall(requestBody)
            .exceptionally(ex -> {
                String errorMsg = "Error during code analysis: " + ex.getMessage();
                System.err.println(errorMsg);
                return errorMsg;
            });
    }

    public CompletableFuture<String> refactorCode(String code) {
        JSONObject requestBody = createRequestPayload(
            "You are a Java refactoring expert. Your task is to suggest improvements for the given code. " +
            "Provide your response in the following format:\n" +
            "1. First, list the suggested improvements with explanations\n" +
            "2. Then, provide the refactored code wrapped in ```java and ``` tags\n" +
            "3. Include all necessary imports\n" +
            "4. Add comments explaining the changes",
            code
        );

        return makeGroqApiCall(requestBody)
            .thenApply(response -> {
                try {
                    // Keep both the explanation and the code
                    return response.trim();
                } catch (Exception e) {
                    System.err.println("Error processing refactor response: " + e.getMessage());
                    throw new GroqApiException("Failed to process refactor response: " + e.getMessage(), e);
                }
            })
            .exceptionally(ex -> {
                String errorMsg = "Error during code refactoring: " + ex.getMessage();
                System.err.println(errorMsg);
                return errorMsg;
            });
    }

    public CompletableFuture<String> sendToGroq(String message) {
        JSONObject requestBody = createRequestPayload(
            "You are a helpful AI assistant. Respond in a clear and concise manner.",
            message
        );

        return makeGroqApiCall(requestBody)
            .exceptionally(ex -> {
                String errorMsg = "Error communicating with Groq: " + ex.getMessage();
                System.err.println(errorMsg);
                return errorMsg;
            });
    }

    @Override
    protected JSONObject createRequestPayload(String systemMessage, String userMessage) {
        JSONObject payload = super.createRequestPayload(systemMessage, userMessage);
        // Add any code generation specific parameters here
        return payload;
    }
}
