package com.neuralcode.insight.exception;

public class InvalidRepositoryUrlException extends RuntimeException {
    public InvalidRepositoryUrlException(String url, String message) {
        super("Invalid repository URL '" + url + "': " + message);
    }
}
