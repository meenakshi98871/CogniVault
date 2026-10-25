
package com.cognivault.agent;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;

import java.util.Map;

@Service
public class QueryUnderstandingAgent {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public QueryUnderstandingAgent() {
    this.restClient = RestClient.builder()
            .baseUrl("http://localhost:11434")
            .build();

    this.objectMapper = new ObjectMapper();
}

    public Map<String, Object> understandQuery(String question) {

        String prompt = """
                You are CogniVault's Query Understanding Agent.

                Analyze the user's question. Do not answer it.

                Perform these tasks:
                1. Identify the user's intent.
                2. Extract important entities and topics.
                3. Analyze the type of task.
                4. Rewrite the question as a clear search query
                   while preserving its original meaning.

                Return only valid JSON with these fields:
                {
                  "intent": "short description",
                  "entities": ["entity1", "entity2"],
                  "taskType": "simple, comparison, multi-part,
                               follow-up, or complex",
                  "analysis": "what information is needed",
                  "reformulatedQuery": "improved search query"
                }

                USER QUESTION:
                %s
                """.formatted(question);

        Map<String, Object> request = Map.of(
                "model", "llama3.2:3b",
                "prompt", prompt,
                "stream", false,
                "format", "json"
        );

        Map<?, ?> response = restClient.post()
                .uri("/api/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(Map.class);

        if (response == null || response.get("response") == null) {
            throw new IllegalStateException(
                    "Ollama returned an empty response");
        }

        try {
            return objectMapper.readValue(
                    response.get("response").toString(),
                    new TypeReference<Map<String, Object>>() {}
            );
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Could not parse query analysis JSON", e);
        }
    }
}
