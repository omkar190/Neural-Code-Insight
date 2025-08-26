package com.neuralcode.insight.service;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.CompletedDirectoryUpload;
import software.amazon.awssdk.transfer.s3.model.DirectoryUpload;
import software.amazon.awssdk.transfer.s3.model.UploadDirectoryRequest;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@Service
public class S3StorageService {

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${AWS_DEFAULT_REGION}")
    private String awsRegion;

    private S3AsyncClient s3AsyncClient;
    private S3TransferManager transferManager;

    @PostConstruct
    public void initialize() {
        s3AsyncClient = S3AsyncClient.crtBuilder()
                    .region(Region.of(awsRegion))
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();

            transferManager = S3TransferManager.builder()
                    .s3Client(s3AsyncClient)
                    .build();

    }

    @PreDestroy
    public void cleanup() {
        if (transferManager != null) {
            transferManager.close();
        }
        if (s3AsyncClient != null) {
            s3AsyncClient.close();
        }
    }

    public Mono<String> uploadRepository(String localPath, String repositoryKey) {
        return Mono.fromCallable(() -> {
                    Path sourcePath = Paths.get(localPath);

                    // Upload entire directory to S3
                    DirectoryUpload directoryUpload = transferManager.uploadDirectory(
                            UploadDirectoryRequest.builder()
                                    .source(sourcePath)
                                    .bucket(bucketName)
                                    .s3Prefix(repositoryKey + "/")
                                    .build()
                    );

                    // Wait for completion
                    CompletedDirectoryUpload completed = directoryUpload.completionFuture().join();

                    // Check for failed transfers
                    if (!completed.failedTransfers().isEmpty()) {
                        throw new RuntimeException("Failed to upload some files: " +
                                completed.failedTransfers().size() + " files failed");
                    }

                    // Return S3 location
                    return String.format("s3://%s/%s/", bucketName, repositoryKey);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(s3Location ->
                        System.out.println("Repository uploaded to: " + s3Location))
                .doOnError(error ->
                        System.err.println("Failed to upload to S3: " + error.getMessage()));
    }

    public Mono<Void> cleanupLocalDirectory(String localPath) {
        return Mono.fromRunnable(() -> {
                    try {
                        File directory = new File(localPath);
                        if (directory.exists()) {
                            FileUtils.deleteDirectory(directory);
                            System.out.println("Directory completely deleted: " + localPath);
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to delete directory: " + e.getMessage());
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}
