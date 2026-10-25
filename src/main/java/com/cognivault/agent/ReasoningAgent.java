
package com.cognivault.agent;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class ReasoningAgent {

    private final RestClient restClient;

    public ReasoningAgent() {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:11434")
                .build();
    }

    public String reason(String question, String context) {

        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException(
                    "Question cannot be empty");
        }

        if (context == null || context.isBlank()) {
            return "The provided documents do not contain "
                    + "enough information to answer this question.";
        }

        String prompt = """
                You are CogniVault's Reasoning Agent.

                Analyze the user's question using only the
                evidence provided in the context.

                Rules:
                1. Identify the facts relevant to the question.
                2. Connect facts when multiple pieces of evidence
                   are needed.
                3. For comparisons, identify similarities and
                   differences supported by the evidence.
                4. Do not invent facts or unsupported conclusions.
                5. If the evidence is insufficient, clearly say so.
                6. Do not claim to have searched any documents.
                7. Return a clear analysis that can be used
                   by the answer-generation component.

                CONTEXT:
                %s

                QUESTION:
                %s

                REASONING:
                """.formatted(context, question);

        Map<String, Object> request = Map.of(
                "model", "llama3.2:3b",
                "prompt", prompt,
                "stream", false
        );

        Map<?, ?> response = restClient.post()
                .uri("/api/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(Map.class);

        if (response == null || response.get("response") == null) {
            throw new IllegalStateException(
                    "Ollama returned an empty reasoning response");
        }

        return response.get("response").toString().trim();
    }
}
