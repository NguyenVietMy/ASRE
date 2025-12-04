package com.asre.asre.api.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyResponse {
    private String apiKey; // Full key shown only at creation/regeneration
    private String maskedApiKey; // Masked version for display
}

