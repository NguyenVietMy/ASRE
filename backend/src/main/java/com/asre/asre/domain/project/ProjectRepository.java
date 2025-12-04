package com.asre.asre.domain.project;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository {
    Optional<Project> findById(UUID id);

    Optional<Project> findByIdAndNotDeleted(UUID id);

    Optional<Project> findByApiKey(String apiKey);

    List<Project> findByOwnerUserId(UUID ownerUserId);

    Project save(Project project);
}
