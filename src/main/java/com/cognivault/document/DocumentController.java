
package com.cognivault.document;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final TextChunkingService chunkingService;
    private final DocumentStore documentStore;
    private final EmbeddingService embeddingService;

    public DocumentController(
        TextChunkingService chunkingService,
        DocumentStore documentStore,
        EmbeddingService embeddingService) {
    this.chunkingService = chunkingService;
    this.documentStore = documentStore;
    this.embeddingService = embeddingService;
}

    @GetMapping("/health")
    public String health() {
        return "Document service is running!";
    }

    @PostMapping(value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadDocument(
            @RequestParam("file") MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            return "Please upload a non-empty file.";
        }

        String content = new String(
                file.getBytes(), StandardCharsets.UTF_8);

        List<DocumentChunk> chunks = chunkingService.splitIntoChunks(
        content, file.getOriginalFilename());
        for (DocumentChunk chunk : chunks) {
    List<Double> embedding =
            embeddingService.generateEmbedding(chunk.getContent());

    documentStore.saveEmbedding(chunk.getId(), embedding);
}

documentStore.saveChunks(chunks);

        return "Document received: " + file.getOriginalFilename()
                + "\nCharacters extracted: " + content.length()
                + "\nChunks created: " + chunks.size()
                + "\n\nFirst chunk:\n" + chunks.get(0).getContent();
    }

            @GetMapping
        public List<DocumentChunk> getAllDocuments() {
            return documentStore.getAllChunks();
        }

        @GetMapping("/count")
        public int getChunkCount() {
            return documentStore.getChunkCount();
        }

        @GetMapping("/embeddings/count")
        public int getEmbeddingCount() {
            return documentStore.getAllEmbeddings().size();
        }
        
}
