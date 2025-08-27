package com.neuralcode.insight.service;

import com.neuralcode.insight.entity.CodeAnalysis;
import com.neuralcode.insight.exception.AnalysisNotFoundException;
import com.neuralcode.insight.repository.CodeAnalysisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisService {

    private final CodeAnalysisRepository analysisRepository;
    private final RepositoryService repositoryService;

    public Mono<CodeAnalysis> startAnalysis(String repositoryUrl, String branchName) {
        String analysisId = UUID.randomUUID().toString();

        CodeAnalysis analysis = new CodeAnalysis(
                analysisId, repositoryUrl, branchName, "STARTED", LocalDateTime.now()
        );

        return analysisRepository.save(analysis)
                .flatMap(saved -> {
                    saved.markNotNew();

                    return repositoryService.cloneAndStoreRepository(
                                    repositoryUrl, branchName, analysisId)  // Pass analysis ID for S3 folder
                            .flatMap(location -> {
                                saved.setStatus("STORED_IN_LOCAL");
                                return analysisRepository.save(saved);
                            })
                            .onErrorResume(error -> {
                                log.error("Analysis {} failed: {}", analysisId, error.getMessage(), error);

                                saved.setStatus("ERROR");
                                saved.setErrorMessage(error.getMessage());
                                saved.setEndTime(LocalDateTime.now());

                                return analysisRepository.save(saved)
                                        .doOnSuccess(updatedAnalysis ->
                                                log.info("Updated analysis {} status to ERROR", analysisId))
                                        .then(Mono.error(error)); // Still propagate the error
                            })
                            ;
                });
    }

    public Mono<CodeAnalysis> getAnalysis(String analysisId) {
        return analysisRepository.findById(analysisId)
                .switchIfEmpty(Mono.error(new AnalysisNotFoundException(analysisId)));
    }

    public Mono<Long> getAnalysisCount() {
        return analysisRepository.count();
    }

    public Mono<CodeAnalysis> updateAnalysisStatus(String analysisId, String status) {
        return analysisRepository.findById(analysisId)
                .flatMap(analysis -> {
                    analysis.setStatus(status);
                    if ("COMPLETED".equals(status)) {
                        analysis.setEndTime(LocalDateTime.now());
                    }
                    return analysisRepository.save(analysis);
                });
    }

    public Flux<CodeAnalysis> getAllAnalyses() {
        return analysisRepository.findAll();
    }

    public Flux<CodeAnalysis> getAnalysisForUrl(String Url) {
        return analysisRepository.findByRepositoryUrl(Url)
                .switchIfEmpty(Flux.error(new AnalysisNotFoundException(Url)));
    }
}
