package com.example.annotationextractor.application;

import com.example.annotationextractor.domain.model.Team;
import com.example.annotationextractor.domain.port.TeamPort;

import java.util.List;

public class TeamQueryService {

    private final TeamPort teamPort;

    public TeamQueryService(TeamPort teamPort) {
        this.teamPort = teamPort;
    }

    public List<Team> listAll() {
        return teamPort.findAll();
    }

    public long count() { return teamPort.count(); }
}


