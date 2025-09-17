package com.example.annotationextractor.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain model representing a team that owns repositories.
 */
public final class Team {

    private final Long id;
    private final String teamName;
    private final String teamCode;
    private final String department;
    private final Instant createdDate;
    private final Instant lastUpdatedDate;

    public Team(Long id,
                String teamName,
                String teamCode,
                String department,
                Instant createdDate,
                Instant lastUpdatedDate) {
        this.id = id;
        this.teamName = teamName;
        this.teamCode = teamCode;
        this.department = department;
        this.createdDate = createdDate;
        this.lastUpdatedDate = lastUpdatedDate;
    }

    public Long getId() { return id; }
    public String getTeamName() { return teamName; }
    public String getTeamCode() { return teamCode; }
    public String getDepartment() { return department; }
    public Instant getCreatedDate() { return createdDate; }
    public Instant getLastUpdatedDate() { return lastUpdatedDate; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return Objects.equals(id, team.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}


