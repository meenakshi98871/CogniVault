
package com.cognivault.document;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DocumentStore {

    private final JdbcTemplate jdbcTemplate;

    public DocumentStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void saveChunkWithEmbedding(
            DocumentChunk chunk, List<Double> embedding) {

        if (embedding == null || embedding.size() != 768) {
            throw new IllegalArgumentException(
                    "Embedding must contain exactly 768 dimensions.");
        }

        Long documentId = jdbcTemplate.query(
                """
                SELECT id
                FROM documents
                WHERE name = ?
                ORDER BY id
                LIMIT 1
                """,
                rs -> rs.next() ? rs.getLong("id") : null,
                chunk.getDocumentName()
        );

        if (documentId == null) {
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement statement =
                        connection.prepareStatement(
                                """
                                INSERT INTO documents (name)
                                VALUES (?)
                                """,
                                new String[]{"id"}
                        );

                statement.setString(1, chunk.getDocumentName());
                return statement;
            }, keyHolder);

            Number key = keyHolder.getKey();

            if (key == null) {
                throw new IllegalStateException(
                        "Could not create document record.");
            }

            documentId = key.longValue();
        }

        String vector = toVectorLiteral(embedding);

        jdbcTemplate.update(
                """
                INSERT INTO document_chunks
                    (document_id, chunk_index, content, embedding)
                VALUES (?, ?, ?, ?::vector)
                """,
                documentId,
                chunk.getChunkIndex(),
                chunk.getContent(),
                vector
        );
    }

    public List<DocumentChunk> getAllChunks() {
        return jdbcTemplate.query(
                """
                SELECT dc.id, d.name AS document_name,
                       dc.chunk_index, dc.content
                FROM document_chunks dc
                JOIN documents d ON d.id = dc.document_id
                ORDER BY dc.id
                """,
                (rs, rowNum) -> new DocumentChunk(
                        String.valueOf(rs.getLong("id")),
                        rs.getString("document_name"),
                        rs.getInt("chunk_index"),
                        rs.getString("content")
                )
        );
    }

    public int getChunkCount() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM document_chunks",
                Integer.class
        );

        return count == null ? 0 : count;
    }

    
    public Map<String, List<Double>> getAllEmbeddings() {
    Map<String, List<Double>> result = new LinkedHashMap<>();

    jdbcTemplate.query(
            """
            SELECT id, embedding::text
            FROM document_chunks
            ORDER BY id
            """,
            rs -> {
                result.put(
                        String.valueOf(rs.getLong("id")),
                        parseVector(rs.getString("embedding"))
                );
            }
    );

    return Map.copyOf(result);
}


    public int getEmbeddingCount() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM document_chunks",
                Integer.class
        );

        return count == null ? 0 : count;
    }

    private String toVectorLiteral(List<Double> embedding) {
        StringBuilder result = new StringBuilder("[");

        for (int i = 0; i < embedding.size(); i++) {
            if (i > 0) {
                result.append(",");
            }

            result.append(embedding.get(i));
        }

        return result.append("]").toString();
    }

    private List<Double> parseVector(String vector) {
        String values = vector.substring(1, vector.length() - 1);
        List<Double> result = new ArrayList<>();

        if (values.isBlank()) {
            return result;
        }

        for (String value : values.split(",")) {
            result.add(Double.parseDouble(value.trim()));
        }

        return result;
    }
}
