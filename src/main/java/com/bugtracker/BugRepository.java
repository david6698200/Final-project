package com.bugtracker;

import java.util.List;
import java.util.Optional;

public interface BugRepository {

    void save(Bug bug);

    Optional<Bug> findById(String id);

    List<Bug> findAll();
}
