package com.neuralcode.insight.service;

import com.neuralcode.insight.exception.InvalidRepositoryUrlException;
import com.neuralcode.insight.exception.RepositoryCloneException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Slf4j
public class RepositoryService {

    public Mono<String> cloneAndStoreRepository(String repositoryUrl, String branchName, String analysisId) {
        return validateRepositoryUrl(repositoryUrl)
                .then(performCloneAndStore(repositoryUrl, branchName, analysisId))
                .timeout(Duration.ofMinutes(10))
                .onErrorResume(TimeoutException.class, ex ->
                        Mono.error(new RepositoryCloneException(analysisId, repositoryUrl,
                                "Repository clone timed out after 10 minutes", ex)))
                .onErrorResume(GitAPIException.class, ex ->
                        Mono.error(new RepositoryCloneException(analysisId, repositoryUrl,
                                "Git operation failed: " + ex.getMessage(), ex)))
                .onErrorResume(IOException.class, ex ->
                        Mono.error(new RepositoryCloneException(analysisId, repositoryUrl,
                                "IO error during clone: " + ex.getMessage(), ex)));
    }

    private Mono<Void> validateRepositoryUrl(String repositoryUrl) {
        return Mono.fromRunnable(() -> {
            if (repositoryUrl == null || repositoryUrl.trim().isEmpty()) {
                throw new InvalidRepositoryUrlException(repositoryUrl, "Repository URL cannot be empty");
            }
        });
    }

    private Mono<String> performCloneAndStore(String repositoryUrl, String branchName, String analysisId) {
        return Mono.fromCallable(() -> {
                    log.info("Starting clone operation for repository: {}, branch: {}, analysisId: {}",
                            repositoryUrl, branchName, analysisId);

                    String projectName = extractProjectNameFromUrl(repositoryUrl);
                    String cleanBranch = cleanBranchName(branchName);
                    String directoryName = String.format("%s-%s-%s", projectName, cleanBranch, analysisId);

                    Path tempDir = Files.createTempDirectory(directoryName);
                    log.debug("Created temporary directory: {}", tempDir);

                    Git git = null;
                    try {
                        git = Git.cloneRepository()
                                .setURI(repositoryUrl)
                                .setDirectory(tempDir.toFile())
                                .setBranch(branchName)
                                .call();

                        log.info("Successfully cloned repository to: {}", tempDir);
                        return tempDir.toString();

                    } catch (GitAPIException e) {
                        log.error("Git clone failed for repository: {}", repositoryUrl, e);
                        throw new RepositoryCloneException(analysisId, repositoryUrl, "Failed to clone repository", e);
                    } finally {
                        if (git != null) {
                            try {
                                git.getRepository().close();
                                git.close();
                                log.debug("Git resources closed for repository: {}", repositoryUrl);
                            } catch (Exception e) {
                                log.warn("Failed to close Git resources", e);
                            }
                        }
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
        //         Removing S3 Upload Logic - Since Only Local Copy is needed
//                .flatMap(pathAndName -> {
//                    String localPath = pathAndName[0];
//                    String directoryName = pathAndName[1];
//
//                    return s3StorageService.uploadRepository(localPath, directoryName)
//                            .flatMap(s3Location ->
//                                    cleanupLocalDirectory(localPath)
//                                            .onErrorResume(ex -> {
//                                                log.warn("Failed to clean up local directory: {}", localPath, ex);
//                                                return Mono.empty();
//                                            })
//                                            .thenReturn(s3Location)
//                            );
//                });
    }

    String extractProjectNameFromUrl(String repositoryUrl) {
        try {
            String url = repositoryUrl.endsWith(".git") ?
                    repositoryUrl.substring(0, repositoryUrl.length() - 4) : repositoryUrl;

            String[] segments = url.split("/");
            String projectName = segments[segments.length - 1];

            // Clean project name
            return projectName.replaceAll("[^a-zA-Z0-9\\-_]", "");

        } catch (Exception e) {
            return "unknown-project";
        }
    }

    String cleanBranchName(String branchName) {
        // Convert branch names like "feature/user-auth" to "feature-user-auth"
        return branchName.replaceAll("[^a-zA-Z0-9\\-_]", "-");
    }

    public Mono<Void> cleanupLocalDirectory(String localPath) {
        return Mono.fromRunnable(() -> {
                    try {
                        File directory = new File(localPath);
                        if (directory.exists()) {
                            FileUtils.deleteDirectory(directory);
                            log.debug("Directory completely deleted: {}", localPath);
                        }
                    } catch (Exception e) {
                        log.error("Failed to delete directory: {}", e.getMessage());
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}
