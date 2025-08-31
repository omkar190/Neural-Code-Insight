package com.neuralcode.insight.dto;

import java.time.LocalDateTime;

public record AnalysisResponse(String analysisId, String status, String repositoryUrl, String branchName, LocalDateTime startTime)
{

    public String getAnalysisId() {
        return analysisId;
    }

    public String getStatus() {
        return status;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }
}
