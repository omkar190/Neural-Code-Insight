package com.neuralcode.insight.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AnalysisRequest(

        @NotBlank(message = "Repository URL is required")
        String repositoryUrl,

        @NotBlank(message = "Branch name is required")
        @Pattern(regexp = "^[\\w\\-\\./_]+$", message = "Invalid branch name format")
        String branchName
) {}
