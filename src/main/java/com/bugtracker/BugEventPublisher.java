package com.bugtracker;

public interface BugEventPublisher {

    void publishBugCreated(String bugId);
}
