package com.asre.asre.domain.project;

import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository {
    Optional<Project> findById(UUID id);

    Optional<Project> findByApiKey(String apiKey);

    Project save(Project project);
}
