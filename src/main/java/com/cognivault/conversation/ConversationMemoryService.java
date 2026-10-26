
package com.cognivault.conversation;

import com.cognivault.agent.MemoryAgent;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConversationMemoryService {

    private final ConversationManager conversationManager;
    private final SlidingWindowManager slidingWindowManager;
    private final MemoryAgent memoryAgent;

    public ConversationMemoryService(
            ConversationManager conversationManager,
            SlidingWindowManager slidingWindowManager,
            MemoryAgent memoryAgent) {

        this.conversationManager = conversationManager;
        this.slidingWindowManager = slidingWindowManager;
        this.memoryAgent = memoryAgent;
    }

    public synchronized boolean processWindowIfNeeded() {
        List<ConversationMessage> messages =
                conversationManager.getMessages();

        if (!slidingWindowManager.shouldSlide(messages)) {
            return false;
        }

        List<ConversationMessage> oldMessages =
                slidingWindowManager.getMessagesToSummarize(messages);

        if (oldMessages.isEmpty()) {
            return false;
        }

        String textToSummarize = oldMessages.stream()
                .map(message ->
                        message.getRole() + ": " + message.getContent())
                .collect(Collectors.joining("\n"));

        // Summarize and persist before modifying the active conversation.
        memoryAgent.createMemory(textToSummarize);

        // Remove only the messages that were successfully summarized and saved.
        List<ConversationMessage> recentMessages =
                slidingWindowManager.getRecentMessages(messages);

        conversationManager.replaceMessages(recentMessages);

        return true;
    }
}
