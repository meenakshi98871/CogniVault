
package com.cognivault.document;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DocumentStore {

    private final Map<String, DocumentChunk> chunks =
            new ConcurrentHashMap<>();

    private final Map<String, List<Double>> embeddings =
            new ConcurrentHashMap<>();

    

    public void saveChunks(List<DocumentChunk> newChunks) {
        for (DocumentChunk chunk : newChunks) {
            chunks.put(chunk.getId(), chunk);
        }
    }

    public List<DocumentChunk> getAllChunks() {
        return List.copyOf(chunks.values());
    }

    public int getChunkCount() {
        return chunks.size();
    }

    public void saveEmbedding(String chunkId, List<Double> embedding) {
        embeddings.put(chunkId, embedding);
    }

    public List<Double> getEmbedding(String chunkId) {
        return embeddings.get(chunkId);
    }

    public Map<String, List<Double>> getAllEmbeddings() {
        return Map.copyOf(embeddings);
    }
}
