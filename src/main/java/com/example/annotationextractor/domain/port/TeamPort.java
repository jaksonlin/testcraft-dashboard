package com.example.annotationextractor.domain.port;

import com.example.annotationextractor.domain.model.Team;
import java.util.List;
import java.util.Optional;

/**
 * Read-only port for accessing teams.
 */
public interface TeamPort {
    Optional<Team> findById(Long id);
    Optional<Team> findByTeamCode(String teamCode);
    List<Team> findAll();
    long count();
}


