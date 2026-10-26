
package com.cognivault.document;
import com.cognivault.agent.MemoryAgent;
import com.cognivault.agent.MemoryRepository;
import com.cognivault.agent.PlanningAgent;
import com.cognivault.agent.QueryUnderstandingAgent;
import com.cognivault.agent.ReasoningAgent;
import com.cognivault.agent.RetrievalAgent;
import com.cognivault.conversation.ConversationManager;
import com.cognivault.conversation.ConversationMemoryService;
import com.cognivault.conversation.ConversationMessage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final TextChunkingService chunkingService;
    private final DocumentStore documentStore;
    private final EmbeddingService embeddingService;
    private final SemanticSearchService semanticSearchService;
    private final AnswerGenerationService answerGenerationService;
    private final QueryUnderstandingAgent queryUnderstandingAgent;
    private final PlanningAgent planningAgent;
    private final RetrievalAgent retrievalAgent;
    private final ReasoningAgent reasoningAgent;
    private final MemoryAgent memoryAgent;
    private final MemoryRepository memoryRepository;
    private final ConversationManager conversationManager;
    private final ConversationMemoryService conversationMemoryService;

    public DocumentController(
        TextChunkingService chunkingService,
        DocumentStore documentStore,
        EmbeddingService embeddingService,
        SemanticSearchService semanticSearchService,
        AnswerGenerationService answerGenerationService,
        QueryUnderstandingAgent queryUnderstandingAgent,
        PlanningAgent planningAgent,
        RetrievalAgent retrievalAgent,
        ReasoningAgent reasoningAgent,
        MemoryAgent memoryAgent,
    MemoryRepository memoryRepository,
    ConversationManager conversationManager,
    ConversationMemoryService conversationMemoryService) {
    this.chunkingService = chunkingService;
    this.documentStore = documentStore;
    this.embeddingService = embeddingService;
    this.semanticSearchService = semanticSearchService;
    this.answerGenerationService = answerGenerationService;
    this.queryUnderstandingAgent = queryUnderstandingAgent;
    this.planningAgent = planningAgent;
    this.retrievalAgent = retrievalAgent;
    this.reasoningAgent = reasoningAgent;
    this.memoryAgent = memoryAgent;
    this.memoryRepository = memoryRepository;
    this.conversationManager = conversationManager;
    this.conversationMemoryService = conversationMemoryService;
    
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

    documentStore.saveChunkWithEmbedding(chunk, embedding);
}



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
            return documentStore.getEmbeddingCount();
        }

        @GetMapping("/search")
        public List<DocumentChunk> searchDocuments(
                @RequestParam("q") String query,
                @RequestParam(defaultValue = "3") int topK) {

            return semanticSearchService.search(query, topK);
        }

        @GetMapping("/ask")
public String askQuestion(@RequestParam("q") String question) {

    List<DocumentChunk> relevantChunks =
            semanticSearchService.search(question, 3);

    if (relevantChunks.isEmpty()) {
        return "No relevant document chunks found.";
    }

    String context = relevantChunks.stream()
            .map(DocumentChunk::getContent)
            .reduce("", (first, second) -> first + "\n" + second);

    return answerGenerationService.generateAnswer(
            question, context);
}

@GetMapping("/understand")
public Map<String, Object> understandQuery(
        @RequestParam("q") String question) {

    return queryUnderstandingAgent.understandQuery(question);
}

@GetMapping("/plan")
public Map<String, Object> createPlan(
        @RequestParam("q") String question) {

    Map<String, Object> analysis =
            queryUnderstandingAgent.understandQuery(question);

    return planningAgent.createPlan(analysis);
}
@GetMapping("/retrieve")
public Map<String, Object> retrieveDocuments(
        @RequestParam("q") String query,
        @RequestParam(defaultValue = "3") int topK) {

    return retrievalAgent.retrieve(query, topK);
}
@GetMapping("/reason")
public String reasonAboutDocuments(
        @RequestParam("q") String question) {

    Map<String, Object> retrieval =
            retrievalAgent.retrieve(question, 3);

    String context = (String) retrieval.get("context");

    return reasoningAgent.reason(question, context);
}

@PostMapping("/memory/summarize")
public Map<String, String> summarizeMemory(
        @RequestBody Map<String, String> request) {

    String information = request.get("text");

    String summary = memoryAgent.createMemory(information);

    return Map.of("summary", summary);
}


@GetMapping("/memory")
public java.util.List<java.util.Map<String, Object>> getMemories() {
    return memoryRepository.getAllMemories();
}

@PostMapping("/conversation/message")
public Map<String, Object> addConversationMessage(
        @RequestBody Map<String, String> request) {

    String role = request.get("role");
    String content = request.get("content");

    conversationManager.addMessage(role, content);

    boolean windowProcessed =
            conversationMemoryService.processWindowIfNeeded();

    return Map.of(
            "message", "Message added successfully.",
            "windowProcessed", windowProcessed,
            "activeMessageCount",
            conversationManager.getMessages().size()
    );
}

@GetMapping("/conversation/messages")
public List<ConversationMessage> getConversationMessages() {
    return conversationManager.getMessages();
}


}
