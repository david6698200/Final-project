package com.bugtracker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BugServiceTest {

    private BugService service;

    @BeforeEach
    void setUp() {
        service = new BugService(new InMemoryBugRepository());
    }

    @Test
    void createValidBug_isOpenAndCanBeReadBack() {
        Bug created = service.create("Login fails", "500 on submit", "HIGH");

        assertNotNull(created.getId());
        assertEquals(Bug.Status.OPEN, created.getStatus());
        assertEquals(Bug.Severity.HIGH, created.getSeverity());
        assertFalse(created.isNotified());

        Bug fetched = service.get(created.getId());
        assertEquals("Login fails", fetched.getTitle());
    }

    @Test
    void createWithoutTitle_throwsValidation() {
        assertThrows(ValidationException.class, () -> service.create(null, "desc", "LOW"));
    }

    @Test
    void createWithBlankTitle_throwsValidation() {
        assertThrows(ValidationException.class, () -> service.create("   ", "desc", "LOW"));
    }

    @Test
    void createWithInvalidSeverity_throwsValidation() {
        assertThrows(ValidationException.class, () -> service.create("x", "desc", "BANANA"));
    }

    @Test
    void createWithoutSeverity_throwsValidation() {
        assertThrows(ValidationException.class, () -> service.create("x", "desc", null));
    }

    @Test
    void severityIsCaseInsensitive() {
        Bug bug = service.create("x", "desc", "medium");
        assertEquals(Bug.Severity.MEDIUM, bug.getSeverity());
    }

    @Test
    void getUnknownId_throwsNotFound() {
        assertThrows(NotFoundException.class, () -> service.get("does-not-exist"));
    }

    @Test
    void updateStatus_changesStatus() {
        Bug bug = service.create("x", "desc", "LOW");

        Bug updated = service.updateStatus(bug.getId(), "RESOLVED");

        assertEquals(Bug.Status.RESOLVED, updated.getStatus());
        assertEquals(Bug.Status.RESOLVED, service.get(bug.getId()).getStatus());
    }

    @Test
    void updateStatusWithInvalidValue_throwsValidation() {
        Bug bug = service.create("x", "desc", "LOW");
        assertThrows(ValidationException.class, () -> service.updateStatus(bug.getId(), "DONE"));
    }

    @Test
    void updateStatusOfUnknownBug_throwsNotFound() {
        assertThrows(NotFoundException.class, () -> service.updateStatus("nope", "RESOLVED"));
    }

    @Test
    void list_returnsAllCreatedBugs() {
        service.create("first", "a", "LOW");
        service.create("second", "b", "HIGH");

        assertEquals(2, service.list().size());
    }
}
