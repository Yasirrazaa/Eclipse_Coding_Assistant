package com.copilot.groq.util;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ChatHistory {
    private static ChatHistory instance;
    private final ConcurrentHashMap<String, ConversationData> conversations;

    private ChatHistory() {
        this.conversations = new ConcurrentHashMap<>();
    }

    public static synchronized ChatHistory getInstance() {
        if (instance == null) {
            instance = new ChatHistory();
        }
        return instance;
    }

    public static void addMessageToConversation(String conversationId, String role, String content) {
        Message message = new Message(role, content);
        getInstance().conversations.computeIfAbsent(conversationId, k -> new ConversationData()).messages.add(message);
    }

    public static void createNewConversation(String conversationId, String title) {
        getInstance().conversations.put(conversationId, new ConversationData(title));
    }

    public static List<String> getAllConversations() {
        return new ArrayList<>(getInstance().conversations.keySet());
    }

    public static String getConversationTitle(String conversationId) {
        ConversationData data = getInstance().conversations.get(conversationId);
        return data != null ? data.title : "Untitled Conversation";
    }

    public static List<Message> getConversationMessages(String conversationId) {
        ConversationData data = getInstance().conversations.get(conversationId);
        return data != null ? new ArrayList<>(data.messages) : new ArrayList<>();
    }

    public static class Message {
        public final String role;
        public final String content;
        public final LocalDateTime timestamp;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
            this.timestamp = LocalDateTime.now();
        }
    }

    private static class ConversationData {
        String title;
        List<Message> messages;

        ConversationData() {
            this("Untitled Conversation");
        }

        ConversationData(String title) {
            this.title = title;
            this.messages = new ArrayList<>();
        }
    }
}
