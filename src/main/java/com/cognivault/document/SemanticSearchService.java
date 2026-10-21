
package com.cognivault.document;

import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class SemanticSearchService {

    private final EmbeddingService embeddingService;
    private final DocumentStore documentStore;

    public SemanticSearchService(
            EmbeddingService embeddingService,
            DocumentStore documentStore) {
        this.embeddingService = embeddingService;
        this.documentStore = documentStore;
    }

    public List<DocumentChunk> search(String query, int topK) {

        List<Double> queryEmbedding =
                embeddingService.generateEmbedding(query);

        Map<String, List<Double>> storedEmbeddings =
                documentStore.getAllEmbeddings();

        return storedEmbeddings.entrySet().stream()
                .map(entry -> Map.entry(
                        entry.getKey(),
                        cosineSimilarity(
                                queryEmbedding, entry.getValue())))
                .sorted(Map.Entry.<String, Double>comparingByValue(
                        Comparator.reverseOrder()))
                .limit(topK)
                .map(entry -> documentStore.getAllChunks().stream()
                        .filter(chunk -> chunk.getId()
                                .equals(entry.getKey()))
                        .findFirst()
                        .orElse(null))
                .filter(chunk -> chunk != null)
                .toList();
    }

    private double cosineSimilarity(
            List<Double> first, List<Double> second) {

        if (first.size() != second.size()) {
            throw new IllegalArgumentException(
                    "Embedding dimensions do not match");
        }

        double dotProduct = 0.0;
        double firstMagnitude = 0.0;
        double secondMagnitude = 0.0;

        for (int i = 0; i < first.size(); i++) {
            double a = first.get(i);
            double b = second.get(i);

            dotProduct += a * b;
            firstMagnitude += a * a;
            secondMagnitude += b * b;
        }

        if (firstMagnitude == 0 || secondMagnitude == 0) {
            return 0.0;
        }

        return dotProduct
                / (Math.sqrt(firstMagnitude)
                * Math.sqrt(secondMagnitude));
    }
}
