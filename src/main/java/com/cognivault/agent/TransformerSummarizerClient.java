
package com.cognivault.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Service
public class TransformerSummarizerClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public TransformerSummarizerClient() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public String summarize(String text) {
        try {
            String jsonBody = objectMapper.writeValueAsString(
                    Map.of("text", text)
            );

            System.out.println(
                    "JSON being sent to Python: " + jsonBody
            );

           HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("http://127.0.0.1:8000/summarize"))
        .version(HttpClient.Version.HTTP_1_1)
        .header("Content-Type", "application/json")
        .header("Accept", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(
                jsonBody,
                java.nio.charset.StandardCharsets.UTF_8
        ))
        .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            System.out.println(
                    "Python response status: "
                            + response.statusCode()
            );

            System.out.println(
                    "Python response body: "
                            + response.body()
            );

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {
                throw new IllegalStateException(
                        "Python summarizer returned HTTP "
                                + response.statusCode()
                                + ": " + response.body()
                );
            }

            Map<?, ?> result = objectMapper.readValue(
                    response.body(),
                    Map.class
            );

            if (result.get("summary") == null) {
                throw new IllegalStateException(
                        "Python summarizer returned no summary."
                );
            }

            return result.get("summary").toString();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to create memory summary: "
                            + e.getMessage(),
                    e
            );
        }
    }
}
