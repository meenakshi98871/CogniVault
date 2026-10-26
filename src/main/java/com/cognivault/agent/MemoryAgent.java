
package com.cognivault.agent;

import org.springframework.stereotype.Service;

@Service
public class MemoryAgent {

    private final TransformerSummarizerClient summarizerClient;
    private final MemoryRepository memoryRepository;

    public MemoryAgent(
            TransformerSummarizerClient summarizerClient,
            MemoryRepository memoryRepository) {
        this.summarizerClient = summarizerClient;
        this.memoryRepository = memoryRepository;
    }

    public String createMemory(String information) {
        if (information == null || information.isBlank()) {
            throw new IllegalArgumentException(
                    "Information for memory cannot be empty.");
        }

        String summary = summarizerClient.summarize(information);

        memoryRepository.saveMemory(summary);

        return summary;
    }
}
