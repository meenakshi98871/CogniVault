
package com.cognivault.agent;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

@Service
public class PlanningAgent {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public PlanningAgent() {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:11434")
                .build();

        this.objectMapper = new ObjectMapper();
    }

    public Map<String, Object> createPlan(
            Map<String, Object> queryAnalysis) {

        String analysisJson;
        try {
            analysisJson = objectMapper.writeValueAsString(
                    queryAnalysis);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Could not serialize query analysis", e);
        }

        String prompt = """
                You are CogniVault's Planning Agent.

                Create an execution plan from the query analysis.
                Do not answer the user's question.

                Available agents:
                - Retrieval Agent: searches document chunks.
                - Tool Agent: uses approved tools when needed.
                - Reasoning Agent: analyzes and compares evidence.
                - Memory Agent: retrieves relevant conversation memory.
                - Validation Agent: checks the final answer against evidence.

                Rules:
                1. Use retrieval for questions about uploaded documents.
                2. Use memory only when conversation history is relevant.
                3. Use tools only when the task requires an available tool.
                4. Keep the plan simple and avoid unnecessary agents.
                5. Independent steps may run in parallel.
                6. Include validation before returning the final answer.
                7. Do not invent tools or claim that steps have executed.

                Return only valid JSON in this structure:
                {
                  "goal": "the user's goal",
                  "steps": [
                    {
                      "step": 1,
                      "agent": "Retrieval Agent",
                      "action": "what this agent should do",
                      "dependsOn": []
                    }
                  ],
                  "executionStrategy": "sequential or parallel",
                  "requiresRetrieval": true,
                  "requiresMemory": false,
                  "requiresTools": false
                }

                QUERY ANALYSIS:
                %s
                """.formatted(analysisJson);

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
                    "Ollama returned an empty planning response");
        }

        try {
            Map<String, Object> plan = objectMapper.readValue(
                    response.get("response").toString(),
                    new TypeReference<Map<String, Object>>() {}
            );

            validatePlan(plan);
            return plan;

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Could not parse or validate execution plan", e);
        }
    }

    private void validatePlan(Map<String, Object> plan) {
        if (!(plan.get("goal") instanceof String)
                || !(plan.get("steps") instanceof List<?>)
                || ((List<?>) plan.get("steps")).isEmpty()) {
            throw new IllegalStateException(
                    "Planning Agent returned an invalid plan");
        }

        for (Object item : (List<?>) plan.get("steps")) {
            if (!(item instanceof Map<?, ?> step)
                    || !(step.get("agent") instanceof String)
                    || !(step.get("action") instanceof String)
                    || !(step.get("dependsOn") instanceof List<?>)) {
                throw new IllegalStateException(
                        "Planning Agent returned an invalid step");
            }
        }
    }
}
