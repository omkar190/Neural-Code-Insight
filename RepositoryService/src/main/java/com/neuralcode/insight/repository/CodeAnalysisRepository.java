package com.neuralcode.insight.repository;

import com.neuralcode.insight.entity.CodeAnalysis;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface CodeAnalysisRepository extends ReactiveCrudRepository<CodeAnalysis, String> {

    Flux<CodeAnalysis> findByRepositoryUrl(String repositoryUrl);

    Flux<CodeAnalysis> findByStatus(String status);
}
