
package com.cognivault.document;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SemanticSearchService {

    private final EmbeddingService embeddingService;
    private final JdbcTemplate jdbcTemplate;

    public SemanticSearchService(
            EmbeddingService embeddingService,
            JdbcTemplate jdbcTemplate) {
        this.embeddingService = embeddingService;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<DocumentChunk> search(String query, int topK) {

        if (query == null || query.isBlank() || topK <= 0) {
            return List.of();
        }

        List<Double> queryEmbedding =
                embeddingService.generateEmbedding(query);

        if (queryEmbedding.size() != 768) {
            throw new IllegalArgumentException(
                    "Query embedding must contain 768 dimensions.");
        }

        StringBuilder vector = new StringBuilder("[");

        for (int i = 0; i < queryEmbedding.size(); i++) {
            if (i > 0) {
                vector.append(",");
            }
            vector.append(queryEmbedding.get(i));
        }

        vector.append("]");

        return jdbcTemplate.query(
                """
                SELECT dc.id, d.name AS document_name,
                       dc.chunk_index, dc.content
                FROM document_chunks dc
                JOIN documents d ON d.id = dc.document_id
                ORDER BY dc.embedding <=> ?::vector
                LIMIT ?
                """,
                (rs, rowNum) -> new DocumentChunk(
                        String.valueOf(rs.getLong("id")),
                        rs.getString("document_name"),
                        rs.getInt("chunk_index"),
                        rs.getString("content")
                ),
                vector.toString(),
                topK
        );
    }
}
