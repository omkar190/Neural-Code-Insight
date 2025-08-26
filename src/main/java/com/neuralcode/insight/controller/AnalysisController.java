package com.neuralcode.insight.controller;

import com.neuralcode.insight.dto.AnalysisRequest;
import com.neuralcode.insight.dto.AnalysisResponse;
import com.neuralcode.insight.exception.InvalidAnalysisException;
import com.neuralcode.insight.service.AnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.neuralcode.insight.entity.CodeAnalysis;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @PostMapping("/v1/repository")
    public Mono<AnalysisResponse> startAnalysis(@Valid @RequestBody AnalysisRequest request) {
        return analysisService.startAnalysis(request.repositoryUrl(), request.branchName())
                .map(this::toResponse);
    }

    @GetMapping("/v1/id/{id}")
    public Mono<AnalysisResponse> getAnalysis(@PathVariable String id) {
        if(id == null || id.trim().isEmpty()){
                throw new InvalidAnalysisException("Please provide valid analysis ID.");
        }
        return analysisService.getAnalysis(id)
                .map(this::toResponse);
    }

    @GetMapping("/v1/url/{url}")
    public Flux<AnalysisResponse> getAnalysisForRepositoryURL(@PathVariable("url") String url){
        if(url == null || url.trim().isEmpty()){
            throw new InvalidAnalysisException("Please provide valid Repository Url.");
        }
        return analysisService.getAnalysisForUrl(url)
                .map(this::toResponse);
    }

    private AnalysisResponse toResponse(CodeAnalysis analysis) {
        return new AnalysisResponse(
                analysis.getId(),
                analysis.getStatus(),
                analysis.getRepositoryUrl(),
                analysis.getBranchName(),
                analysis.getStartTime()
        );
    }
}
