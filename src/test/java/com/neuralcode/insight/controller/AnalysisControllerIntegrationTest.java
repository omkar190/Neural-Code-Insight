package com.neuralcode.insight.controller;

import com.neuralcode.insight.NeuralCodeInsightApplication;
import com.neuralcode.insight.dto.AnalysisRequest;
import com.neuralcode.insight.dto.AnalysisResponse;
import com.neuralcode.insight.service.RepositoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(
        classes = NeuralCodeInsightApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "AWS_DEFAULT_REGION=eu-north-1"
        }
)
class AnalysisControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private RepositoryService repositoryService;

    @BeforeEach
    void setUp() {
        when(repositoryService.cloneAndStoreRepository(anyString(), anyString(), anyString()))
                .thenReturn(Mono.just("s3://test-bucket/spring-demo-main-abc123/"));
    }

    @Test
    void shouldStartAnalysisSuccessfully() {
        // Given
        AnalysisRequest request = new AnalysisRequest(
                "https://github.com/spring-projects/spring-demo",
                "main"
        );

        // When & Then
        webTestClient.post()
                .uri("/api/analysis/v1/repository")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AnalysisResponse.class)
                .value(response -> {
                    assertThat(response.getAnalysisId()).isNotEmpty();
                    assertThat(response.getStatus()).isEqualTo("STORED_IN_S3");
                    assertThat(response.getRepositoryUrl()).isEqualTo(request.repositoryUrl());
                });
    }

    @Test
    void shouldRejectInvalidRepositoryUrl() {
        // Given
        AnalysisRequest request = new AnalysisRequest("", "main");

        // When & Then
        webTestClient.post()
                .uri("/api/analysis/v1/repository")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldHandleEmptyBranchName() {
        // Given
        AnalysisRequest request = new AnalysisRequest(
                "https://github.com/spring-projects/spring-demo",
                ""
        );

        // When & Then
        webTestClient.post()
                .uri("/api/analysis/v1/repository")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }
}
