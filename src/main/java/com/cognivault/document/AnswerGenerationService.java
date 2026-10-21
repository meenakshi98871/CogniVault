
package com.cognivault.document;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;

import java.util.Map;

@Service
public class AnswerGenerationService {

    private final RestClient restClient;

    public AnswerGenerationService() {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:11434")
                .build();
    }

    public String generateAnswer(String question, String context) {

        String prompt = """
                You are CogniVault, a document question-answering assistant.

                Answer the question using only the supplied context.
                If the context does not contain the answer, say:
                "The provided documents do not contain enough information."

                Context:
                %s

                Question:
                %s

                Answer:
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
                    "Ollama returned an empty response");
        }

        return response.get("response").toString().trim();
    }
}
