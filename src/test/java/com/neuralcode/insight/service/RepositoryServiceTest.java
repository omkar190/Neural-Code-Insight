package com.neuralcode.insight.service;

import com.neuralcode.insight.exception.InvalidRepositoryUrlException;
import com.neuralcode.insight.exception.RepositoryCloneException;
import com.neuralcode.insight.exception.S3UploadException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryServiceTest {

    @Mock
    private S3StorageService s3StorageService;

    @InjectMocks
    private RepositoryService repositoryService;

    @Test
    void shouldExtractProjectNameFromValidUrl() {
        // Given
        String url = "https://github.com/spring-projects/spring-boot";

        // When
        String projectName = repositoryService.extractProjectNameFromUrl(url);

        // Then
        assertThat(projectName).isEqualTo("spring-boot");
    }

    @Test
    void shouldCleanBranchNameWithSpecialCharacters() {
        // Given
        String branchName = "feature/user-auth";

        // When
        String cleanBranch = repositoryService.cleanBranchName(branchName);

        // Then
        assertThat(cleanBranch).isEqualTo("feature-user-auth");
    }

    @Test
    void shouldThrowExceptionForCouldNotCloneRepositoryUrl() {
        // Given
        String invalidUrl = "not-a-valid-url";

        // When & Then
        StepVerifier.create(repositoryService.cloneAndStoreRepository(invalidUrl, "main", "test-id"))
                .expectError(RepositoryCloneException.class)
                .verify();
    }

    @Test
    void shouldThrowExceptionForEmptyRepositoryUrl() {
        // When & Then
        StepVerifier.create(repositoryService.cloneAndStoreRepository("", "main", "test-id"))
                .expectError(InvalidRepositoryUrlException.class)
                .verify();
    }

    @Test
    void shouldHandleS3UploadFailure() {
        // Given
        String validUrl = "https://github.com/spring-projects/spring-petclinic";
        when(s3StorageService.uploadRepository(anyString(), anyString()))
                .thenReturn(Mono.error(new S3UploadException("/tmp/test", "S3 error", new RuntimeException())));

        // When & Then
        StepVerifier.create(repositoryService.cloneAndStoreRepository(validUrl, "main", "test-id"))
                .expectError(S3UploadException.class)
                .verify();
    }
}
