
package com.cognivault.document;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TextChunkingService {

    private static final int CHUNK_SIZE = 500;
    private static final int CHUNK_OVERLAP = 100;

    public List<DocumentChunk> splitIntoChunks(
            String text, String documentName) {

        List<DocumentChunk> chunks = new ArrayList<>();

        if (text == null || text.isBlank()) {
            return chunks;
        }

        int start = 0;
        int chunkIndex = 0;

        while (start < text.length()) {
            int end = Math.min(start + CHUNK_SIZE, text.length());
            String content = text.substring(start, end);

            chunks.add(new DocumentChunk(
                    UUID.randomUUID().toString(),
                    documentName,
                    chunkIndex,
                    content
            ));

            chunkIndex++;

            if (end == text.length()) {
                break;
            }

            start = end - CHUNK_OVERLAP;
        }

        return chunks;
    }
}
