package com.asre.asre.application.project;

import lombok.Value;

import java.util.UUID;

@Value
public class CreateProjectCommand {
    String name;
    String description;
    UUID ownerUserId;
}
