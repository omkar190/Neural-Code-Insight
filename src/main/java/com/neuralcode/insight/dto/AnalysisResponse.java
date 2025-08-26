package com.neuralcode.insight.dto;

import java.time.LocalDateTime;

public record AnalysisResponse(String analysisId, String status, String repositoryUrl, String branchName, LocalDateTime startTime)
{

}
