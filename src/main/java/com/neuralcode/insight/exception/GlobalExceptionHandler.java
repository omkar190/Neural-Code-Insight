package com.neuralcode.insight.exception;

import com.neuralcode.insight.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import java.util.Map;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(RepositoryCloneException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleRepositoryCloneException(RepositoryCloneException ex) {
        log.error("Repository clone failed for URL: {}", ex.getRepositoryUrl(), ex);

        String details = Map.of(
                        "Repository URL", ex.getRepositoryUrl(),
                        "Analysis Id", ex.getAnalysisId()
                ).entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining(", "));


        ErrorResponse error = ErrorResponse.builder()
                .errorCode("REPOSITORY_CLONE_FAILED")
                .message("Failed to clone the provided repository: ")
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error));
    }

    @ExceptionHandler(S3UploadException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleS3UploadException(S3UploadException ex) {
        log.error("S3 upload failed for path: {}", ex.getLocalPath(), ex);

        ErrorResponse error = ErrorResponse.builder()
                .errorCode("S3_UPLOAD_FAILED")
                .message("Failed to upload repository to S3")
                .details("Please try again. If the issue persists, contact support.")
                .timestamp(LocalDateTime.now())
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error));
    }

    @ExceptionHandler(AnalysisNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleAnalysisNotFoundException(AnalysisNotFoundException ex) {
        log.warn("Analysis not found: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .errorCode("ANALYSIS_NOT_FOUND")
                .message("No analysis found for provided scopes.")
                .timestamp(LocalDateTime.now())
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(error));
    }

    @ExceptionHandler(InvalidAnalysisException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleInvalidAnalysisException(InvalidAnalysisException ex) {
        log.warn("Invalid analysis request: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .errorCode("INVALID_ANALYSIS_REQUEST")
                .message("Please provide valid analysis parameters.")
                .timestamp(LocalDateTime.now())
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidationException(WebExchangeBindException ex) {
        log.warn("Validation failed: {}", ex.getMessage());

        String details = ex.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorResponse error = ErrorResponse.builder()
                .errorCode("VALIDATION_FAILED")
                .message("Request validation failed")
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error));
    }

    @ExceptionHandler(ServerWebInputException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleServerWebInputException(ServerWebInputException ex) {
        log.warn("Invalid or missing request body: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .errorCode("INVALID_REQUEST_BODY")
                .message("Request body is required and must be valid JSON")
                .details("Please provide a valid JSON request body with required fields")
                .timestamp(LocalDateTime.now())
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error));
    }

    @ExceptionHandler(DecodingException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleDecodingException(DecodingException ex) {
        log.warn("JSON decoding failed: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .errorCode("INVALID_JSON")
                .message("Invalid JSON format in request body")
                .details("Please check your JSON syntax and try again")
                .timestamp(LocalDateTime.now())
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleResponseStatusException(ResponseStatusException ex) {
        log.warn("HTTP status exception: {} - {}", ex.getStatusCode(), ex.getReason());

        ErrorResponse error = ErrorResponse.builder()
                .errorCode("HTTP_ERROR")
                .message("HTTP error occurred")
                .details(ex.getReason() != null ? ex.getReason() : "Please check your request")
                .timestamp(LocalDateTime.now())
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred: {}", ex.getClass().getSimpleName(), ex);

        ErrorResponse error = ErrorResponse.builder()
                .errorCode("INTERNAL_ERROR")
                .message("An unexpected error occurred. Please contact support.")
                .details("Error type: " + ex.getClass().getSimpleName())
                .timestamp(LocalDateTime.now())
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error));
    }
}
