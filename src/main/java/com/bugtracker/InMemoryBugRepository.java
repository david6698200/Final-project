package com.bugtracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryBugRepository implements BugRepository {

    private final Map<String, Bug> bugs = new ConcurrentHashMap<>();

    @Override
    public void save(Bug bug) {
        bugs.put(bug.getId(), bug);
    }

    @Override
    public Optional<Bug> findById(String id) {
        return Optional.ofNullable(bugs.get(id));
    }

    @Override
    public List<Bug> findAll() {
        return new ArrayList<>(bugs.values());
    }
}
