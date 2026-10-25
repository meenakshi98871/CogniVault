
package com.cognivault.agent;

import com.cognivault.document.DocumentChunk;
import com.cognivault.document.SemanticSearchService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class RetrievalAgent {

    private final SemanticSearchService semanticSearchService;

    public RetrievalAgent(
            SemanticSearchService semanticSearchService) {
        this.semanticSearchService = semanticSearchService;
    }

    public Map<String, Object> retrieve(
            String query, int topK) {

        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException(
                    "Search query cannot be empty");
        }

        int resultLimit = Math.max(1, Math.min(topK, 10));

        List<DocumentChunk> chunks =
                semanticSearchService.search(query, resultLimit);

        String context = chunks.stream()
                .map(DocumentChunk::getContent)
                .reduce("", (first, second) ->
                        first.isEmpty()
                                ? second
                                : first + "\n\n" + second);

        return Map.of(
                "query", query,
                "retrievedChunks", chunks,
                "chunkCount", chunks.size(),
                "context", context
        );
    }
}
