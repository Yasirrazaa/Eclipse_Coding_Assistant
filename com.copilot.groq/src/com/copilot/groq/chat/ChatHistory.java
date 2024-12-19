package com.copilot.groq.chat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ChatHistory {
    private static ChatHistory instance;
    private final ConcurrentHashMap<String, List<ChatMessage>> conversations;

    private ChatHistory() {
        this.conversations = new ConcurrentHashMap<>();
    }

    public static synchronized ChatHistory getInstance() {
        if (instance == null) {
            instance = new ChatHistory();
        }
        return instance;
    }

    public void addMessage(String conversationId, String content, boolean isUser) {
        ChatMessage message = new ChatMessage(content, isUser, LocalDateTime.now());
        conversations.computeIfAbsent(conversationId, k -> new ArrayList<>()).add(message);
    }

    public List<ChatMessage> getConversation(String conversationId) {
        return conversations.getOrDefault(conversationId, new ArrayList<>());
    }

    public List<String> getConversationIds() {
        return new ArrayList<>(conversations.keySet());
    }

    public static class ChatMessage {
        private final String content;
        private final boolean isUser;
        private final LocalDateTime timestamp;

        public ChatMessage(String content, boolean isUser, LocalDateTime timestamp) {
            this.content = content;
            this.isUser = isUser;
            this.timestamp = timestamp;
        }

        public String getContent() {
            return content;
        }

        public boolean isUser() {
            return isUser;
        }

        public String getFormattedTimestamp() {
            return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }
}
