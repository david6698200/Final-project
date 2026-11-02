package com.bugtracker;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class BugService {

    private final BugRepository repository;

    public BugService(BugRepository repository) {
        this.repository = repository;
    }

    public Bug create(String title, String description, String severity) {
        if (title == null || title.isBlank()) {
            throw new ValidationException("title is required");
        }
        Bug.Severity parsedSeverity = parseSeverity(severity);

        Bug bug = new Bug(
                UUID.randomUUID().toString(),
                title.trim(),
                description == null ? "" : description,
                parsedSeverity,
                Bug.Status.OPEN,
                false,
                Instant.now().toString());

        repository.save(bug);
        return bug;
    }

    public Bug get(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("bug not found: " + id));
    }

    public List<Bug> list() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(Bug::getCreatedAt))
                .toList();
    }

    public Bug updateStatus(String id, String status) {
        Bug.Status parsedStatus = parseStatus(status);
        Bug bug = get(id);
        bug.setStatus(parsedStatus);
        repository.save(bug);
        return bug;
    }

    private Bug.Severity parseSeverity(String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("severity is required (LOW, MEDIUM, HIGH)");
        }
        try {
            return Bug.Severity.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("invalid severity: " + value + " (LOW, MEDIUM, HIGH)");
        }
    }

    private Bug.Status parseStatus(String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("status is required (OPEN, IN_PROGRESS, RESOLVED)");
        }
        try {
            return Bug.Status.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("invalid status: " + value + " (OPEN, IN_PROGRESS, RESOLVED)");
        }
    }
}
