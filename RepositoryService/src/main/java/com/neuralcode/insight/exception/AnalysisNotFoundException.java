package com.neuralcode.insight.exception;

public class AnalysisNotFoundException extends RuntimeException {
    public AnalysisNotFoundException(String analysisId) {
        super("Analysis not found for: " + analysisId);
    }
}
