package com.fhtw.meetingnotes_backend.service;
import com.fhtw.meetingnotes_backend.domain.ActionItem;
import com.fhtw.meetingnotes_backend.domain.MeetingNote;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Heuristic parser that extracts action items from free-form meeting notes.
 *
 * Recognises lines that start with common action item markers:
 *   - "[ ]", "- [ ]", "* [ ]"
 *   - "TODO:", "ACTION:", "ACTION ITEM:"
 *   - Numbered bullets followed by a verb-ish pattern
 *
 * For each match it also tries to extract an owner (@name or "owner: name")
 * and a due date (by:, due:, or ISO date).
 */
@Component
public class MeetingNotesParser {

    private static final Pattern ACTION_LINE = Pattern.compile(
            "^\\s*(?:[-*]\\s*\\[\\s*[\\s_]\\s*]|TODO:|ACTION(?:\\s+ITEM)?:|\\d+\\.)\\s*(.+)",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern OWNER_PATTERN = Pattern.compile(
            "@(\\w+)|(?:owner|assigned to|responsible):\\s*(\\S+)",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern DUE_DATE_PATTERN = Pattern.compile(
            "(?:by|due|deadline):\\s*(\\d{4}-\\d{2}-\\d{2}|\\d{1,2}/\\d{1,2}/\\d{2,4})",
            Pattern.CASE_INSENSITIVE
    );

    public List<ActionItem> parse(MeetingNote note) {
        List<ActionItem> items = new ArrayList<>();
        String[] lines = note.getRawContent().split("\\r?\\n");
        for (String line : lines) {
            Matcher m = ACTION_LINE.matcher(line);
            if (!m.matches()) continue;

            String body = m.group(1).trim();
            ActionItem item = new ActionItem();
            item.setMeetingNote(note);
            item.setDescription(body);

            // Try to extract owner
            Matcher om = OWNER_PATTERN.matcher(body);
            if (om.find()) {
                item.setOwner(om.group(1) != null ? om.group(1) : om.group(2));
            }

            // Try to extract due date
            Matcher dm = DUE_DATE_PATTERN.matcher(body);
            if (dm.find()) {
                item.setDueDate(parseDate(dm.group(1)));
            }

            items.add(item);
        }
        return items;
    }

    private LocalDate parseDate(String raw) {
        try {
            if (raw.contains("-")) return LocalDate.parse(raw, DateTimeFormatter.ISO_LOCAL_DATE);
            String[] parts = raw.split("/");
            if (parts.length == 3) {
                int year = Integer.parseInt(parts[2]);
                if (year < 100) year += 2000;
                return LocalDate.of(year, Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
            }
        } catch (Exception ignored) {}
        return null;
    }
}