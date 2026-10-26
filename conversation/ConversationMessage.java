
package com.cognivault.conversation;

import java.time.LocalDateTime;

public class ConversationMessage {

    private final String role;
    private final String content;
    private final LocalDateTime timestamp;

    public ConversationMessage(String role, String content) {
        this.role = role;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }

    public String getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
