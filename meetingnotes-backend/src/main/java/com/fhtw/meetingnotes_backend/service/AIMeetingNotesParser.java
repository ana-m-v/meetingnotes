package com.fhtw.meetingnotes_backend.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fhtw.meetingnotes_backend.domain.ActionItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIMeetingNotesParser {
    private final OllamaClient ollamaClient;
    private final ObjectMapper objectMapper;

    public List<ActionItem> parse(String rawContent) {
        String rawJson = ollamaClient.extractActions(rawContent);

        ExtractionResult result;
        try {
            result = objectMapper.readValue(rawJson, ExtractionResult.class);
            validate(result);
        } catch (Exception firstError) {
            log.warn("AI output invalid, trying repair: {}", firstError.getMessage());
            String repaired = ollamaClient.repairExtraction(rawJson, firstError.getMessage());

            try {
                result = objectMapper.readValue(repaired, ExtractionResult.class);
                validate(result);
            } catch (Exception secondError) {
                throw new RuntimeException("AI extraction failed after repair", secondError);
            }
        }

        List<ActionItem> items = new ArrayList<>();
        for (ExtractedActionItem a : result.actions) {
            ActionItem item = new ActionItem();
            item.setDescription(a.description.trim());
            item.setOwner(blankToNull(a.owner));
            item.setDueDate(parseDate(a.dueDate));
            item.setStatus(parseStatus(a.status));
            items.add(item);
        }
        return items;
    }

    private void validate(ExtractionResult result) {
        if (result == null || result.actions == null) {
            throw new IllegalArgumentException("Missing actions");
        }
        for (ExtractedActionItem a : result.actions) {
            if (a.description == null || a.description.isBlank()) {
                throw new IllegalArgumentException("Missing description");
            }
        }
    }

    private LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return LocalDate.parse(raw.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private ActionItem.Status parseStatus(String raw) {
        if (raw == null || raw.isBlank()) return ActionItem.Status.OPEN;
        try {
            return ActionItem.Status.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            return ActionItem.Status.OPEN;
        }
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ExtractionResult {
        @JsonProperty("actions")
        public List<ExtractedActionItem> actions;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ExtractedActionItem {
        @JsonProperty("description")
        public String description;
        @JsonProperty("owner")
        public String owner;
        @JsonProperty("dueDate")
        public String dueDate;
        @JsonProperty("status")
        public String status;
    }
}
