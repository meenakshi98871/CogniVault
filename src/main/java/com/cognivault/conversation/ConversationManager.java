
package com.cognivault.conversation;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ConversationManager {

    private final List<ConversationMessage> messages = new ArrayList<>();

    public synchronized void addMessage(String role, String content) {
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Message role cannot be empty.");
        }

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Message content cannot be empty.");
        }

        messages.add(new ConversationMessage(role, content));
    }

    public synchronized List<ConversationMessage> getMessages() {
        return Collections.unmodifiableList(new ArrayList<>(messages));
    }

    public synchronized void clearConversation() {
        messages.clear();
    }
    
    public synchronized void replaceMessages(
            List<ConversationMessage> newMessages) {

        messages.clear();
        messages.addAll(newMessages);
    }

}
