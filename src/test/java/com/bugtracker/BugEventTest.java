package com.bugtracker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class BugEventTest {

    @Test
    void createBug_publishesEventWithBugId() {
        List<String> events = new ArrayList<>();
        BugService service = new BugService(new InMemoryBugRepository(), events::add);

        Bug bug = service.create("Login fails", "desc", "HIGH");

        assertEquals(List.of(bug.getId()), events);
    }

    @Test
    void invalidBug_doesNotPublishEvent() {
        List<String> events = new ArrayList<>();
        BugService service = new BugService(new InMemoryBugRepository(), events::add);

        assertThrows(ValidationException.class, () -> service.create("", "desc", "HIGH"));

        assertTrue(events.isEmpty());
    }

    @Test
    void failingPublisher_doesNotBreakCreate() {
        BugService service = new BugService(new InMemoryBugRepository(), bugId -> {
            throw new RuntimeException("queue is down");
        });

        Bug bug = service.create("x", "desc", "LOW");

        assertEquals(bug.getId(), service.get(bug.getId()).getId());
    }

    @Test
    void markNotified_setsFlag() {
        BugService service = new BugService(new InMemoryBugRepository());
        Bug bug = service.create("x", "desc", "LOW");
        assertFalse(bug.isNotified());

        service.markNotified(bug.getId());

        assertTrue(service.get(bug.getId()).isNotified());
    }

    @Test
    void markNotifiedOfUnknownBug_throwsNotFound() {
        BugService service = new BugService(new InMemoryBugRepository());
        assertThrows(NotFoundException.class, () -> service.markNotified("nope"));
    }
}
