package com.neuralcode.insight.exception;
import lombok.Getter;

@Getter
public class S3UploadException extends RuntimeException {
    private final String localPath;

    public S3UploadException(String localPath, String message, Throwable cause) {
        super(message, cause);
        this.localPath = localPath;
    }

}
