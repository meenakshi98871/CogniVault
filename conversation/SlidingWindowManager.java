
package com.cognivault.conversation;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SlidingWindowManager {

    private static final int MAX_MESSAGES = 20;
    private static final int RECENT_MESSAGES_TO_KEEP = 10;

    public boolean shouldSlide(List<ConversationMessage> messages) {
        return messages.size() >= MAX_MESSAGES;
    }

    public List<ConversationMessage> getMessagesToSummarize(
            List<ConversationMessage> messages) {

        if (messages.size() < MAX_MESSAGES) {
            return List.of();
        }

        int messagesToSummarize =
                messages.size() - RECENT_MESSAGES_TO_KEEP;

        return new ArrayList<>(
                messages.subList(0, messagesToSummarize)
        );
    }
    
public List<ConversationMessage> getRecentMessages(
        List<ConversationMessage> messages) {

    if (messages.size() <= RECENT_MESSAGES_TO_KEEP) {
        return new ArrayList<>(messages);
    }

    return new ArrayList<>(
            messages.subList(
                    messages.size() - RECENT_MESSAGES_TO_KEEP,
                    messages.size()
            )
    );
}

}
