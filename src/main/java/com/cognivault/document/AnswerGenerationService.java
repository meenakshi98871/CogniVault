
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

        IMPORTANT RULES:
        1. Answer using only the information in the CONTEXT.
        2. Read the entire context before answering.
        3. If the context directly contains the answer, give it clearly.
        4. For questions asking for multiple items, list each item.
        5. Do not say information is missing when the context contains it.
        6. If the answer truly is not in the context, say:
           "The provided documents do not contain enough information."

        CONTEXT:
        %s

        QUESTION:
        %s

        ANSWER:
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
