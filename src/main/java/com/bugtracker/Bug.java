package com.bugtracker;

public class Bug {

    public enum Severity { LOW, MEDIUM, HIGH }

    public enum Status { OPEN, IN_PROGRESS, RESOLVED }

    private final String id;
    private final String title;
    private final String description;
    private final Severity severity;
    private Status status;
    private boolean notified;
    private final String createdAt;

    public Bug(String id, String title, String description, Severity severity,
               Status status, boolean notified, String createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.status = status;
        this.notified = notified;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Severity getSeverity() { return severity; }
    public Status getStatus() { return status; }
    public boolean isNotified() { return notified; }
    public String getCreatedAt() { return createdAt; }

    public void setStatus(Status status) { this.status = status; }
    public void setNotified(boolean notified) { this.notified = notified; }
}
