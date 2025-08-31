package com.neuralcode.insight.exception;
import lombok.Getter;

@Getter
public class RepositoryCloneException extends RuntimeException {
    private final String repositoryUrl;
    private final String analysisId;

    public RepositoryCloneException(String analysisId, String repositoryUrl, String message, Throwable cause) {
        super(message, cause);
        this.analysisId = analysisId;
        this.repositoryUrl = repositoryUrl;
    }

}
