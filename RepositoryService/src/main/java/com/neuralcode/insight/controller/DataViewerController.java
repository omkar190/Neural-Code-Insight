package com.neuralcode.insight.controller;

import com.neuralcode.insight.entity.CodeAnalysis;
import com.neuralcode.insight.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/debug")
@RequiredArgsConstructor
public class DataViewerController {

    private final AnalysisService analysisService;

    @GetMapping("/analyses")
    public Flux<CodeAnalysis> viewAllAnalyses() {
        return analysisService.getAllAnalyses();
    }

    @GetMapping("/analyses/count")
    public Mono<Long> countAnalyses() {
        return analysisService.getAnalysisCount();
    }

    @GetMapping("/analyses/{id}")
    public Mono<CodeAnalysis> viewSpecificAnalysis(@PathVariable String id) {
        return analysisService.getAnalysis(id);
    }
}
