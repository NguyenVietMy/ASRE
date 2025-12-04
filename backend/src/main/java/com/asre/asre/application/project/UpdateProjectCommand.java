package com.asre.asre.application.project;

import lombok.Value;

import java.util.UUID;

@Value
public class UpdateProjectCommand {
    UUID projectId;
    UUID ownerUserId;
    String name;
    String description;
}

