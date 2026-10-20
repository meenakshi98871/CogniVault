package com.cognivault.document;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

@Service
public class EmbeddingService {

    private final RestClient restClient;

    public EmbeddingService() {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:11434")
                .build();
    }

    public List<Double> generateEmbedding(String text) {
        Map<String, Object> request = Map.of(
                "model", "nomic-embed-text",
                "input", text
        );

        Map response = restClient.post()
                .uri("/api/embed")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(Map.class);

        List<List<Double>> embeddings =
                (List<List<Double>>) response.get("embeddings");

        return embeddings.get(0);
    }
}
