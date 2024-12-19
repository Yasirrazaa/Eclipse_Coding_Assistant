package com.copilot.groq.api;

import java.util.concurrent.CompletableFuture;

import org.json.JSONObject;

public class CodeRefactoringService extends BaseGroqService {

    public static class RefactoringResult {
        private final String refactoredCode;
        private final String message;

        public RefactoringResult(String refactoredCode, String message) {
            this.refactoredCode = refactoredCode;
            this.message = message;
        }

        public String getRefactoredCode() {
            return refactoredCode;
        }

        public String getMessage() {
            return message;
        }
    }

    public CompletableFuture<RefactoringResult> refactorCode(String code, String refactoringType) {
        JSONObject requestBody = createRequestPayload(
            "You are a Java refactoring expert. Your task is to refactor the given code according to best practices. " +
            "Format your response as follows:\n" +
            "1. First, provide the refactored code wrapped in ```java and ``` tags\n" +
            "2. Then, add '---' on a new line\n" +
            "3. Finally, explain the changes you made",
            "Refactoring type: " + refactoringType + "\nCode:\n" + code
        );

        return makeGroqApiCall(requestBody)
            .thenApply(response -> {
                try {
                    // Parse the response content directly since BaseGroqService already extracted it
                    String[] parts = response.split("\n---\n");

                    // Extract code and remove markdown tags
                    String refactoredCode = parts[0];
                    if (refactoredCode.contains("```java")) {
                        refactoredCode = refactoredCode.substring(
                            refactoredCode.indexOf("```java") + 7,
                            refactoredCode.lastIndexOf("```")
                        ).trim();
                    } else if (refactoredCode.contains("```")) {
                        refactoredCode = refactoredCode.substring(
                            refactoredCode.indexOf("```") + 3,
                            refactoredCode.lastIndexOf("```")
                        ).trim();
                    }

                    // Extract message
                    String message = parts.length > 1 ? parts[1].trim() : "Code refactored successfully";

                    // If no code was extracted, use the entire response as code
                    if (refactoredCode.isEmpty()) {
                        refactoredCode = response.trim();
                    }

                    return new RefactoringResult(refactoredCode, message);
                } catch (Exception e) {
                    throw new GroqApiException("Failed to process refactoring response: " + e.getMessage(), e);
                }
            });
    }
}
