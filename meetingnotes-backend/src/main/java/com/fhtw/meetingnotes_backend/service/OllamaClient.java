package com.fhtw.meetingnotes_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;

@Service
public class OllamaClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String model;

    public OllamaClient(
            ObjectMapper objectMapper,
            @Value("${app.ai.ollama-url:http://localhost:11434}") String baseUrl,
            @Value("${app.ai.model:llama3.2:3b}") String model
    ) {
        this.objectMapper = objectMapper;
        this.model = model;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public String extractActions(String rawContent) {
        String schema = """
            {
              "type": "object",
              "properties": {
                "actions": {
                  "type": "array",
                  "items": {
                    "type": "object",
                    "properties": {
                      "description": { "type": "string" },
                      "owner": { "type": ["string", "null"] },
                      "dueDate": { "type": ["string", "null"] },
                      "status": { "type": "string" }
                    },
                    "required": ["description", "owner", "dueDate", "status"]
                  }
                }
              },
              "required": ["actions"]
            }
            """;

        String prompt = """
            Extract actionable tasks from these meeting notes.

            Return ONLY valid JSON matching this schema:
            %s

            Rules:
            - Include only real action items.
            - description is required.
            - owner may be null.
            - dueDate must be YYYY-MM-DD or null.
            - status must be OPEN, IN_PROGRESS, or DONE.
            - If unsure, use OPEN.

            Meeting notes:
            %s
            """.formatted(schema, rawContent);

        return callChat(prompt, schema);
    }

    public String repairExtraction(String brokenOutput, String errorMessage) {
        String schema = """
            {
              "type": "object",
              "properties": {
                "actions": {
                  "type": "array",
                  "items": {
                    "type": "object",
                    "properties": {
                      "description": { "type": "string" },
                      "owner": { "type": ["string", "null"] },
                      "dueDate": { "type": ["string", "null"] },
                      "status": { "type": "string" }
                    },
                    "required": ["description", "owner", "dueDate", "status"]
                  }
                }
              },
              "required": ["actions"]
            }
            """;

        String prompt = """
            Repair this output into valid JSON matching this schema:

            %s

            Error:
            %s

            Broken output:
            %s
            """.formatted(schema, errorMessage, brokenOutput);

        return callChat(prompt, schema);
    }

    private String callChat(String prompt, String schemaJson) {
        Map<String, Object> body = Map.of(
                "model", model,
                "stream", false,
                "format", objectMapper.readTree(schemaJson),
                "messages", List.of(
                        Map.of("role", "system", "content", "You extract structured action items from meeting notes."),
                        Map.of("role", "user", "content", prompt)
                ),
                "keep_alive", "10m"
        );

        String response = restClient.post()
                .uri("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = objectMapper.readTree(response);
            return root.path("message").path("content").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Ollama response", e);
        }
    }
}