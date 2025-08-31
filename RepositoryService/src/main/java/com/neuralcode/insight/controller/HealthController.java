package com.neuralcode.insight.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class HealthController {

    private final DatabaseClient databaseClient;

    @GetMapping("/health")
    public Mono<String> health(){
        return Mono.just("Neural Code Insight Engine is running!");
    }

    @GetMapping("/database")
    public Mono<Map<String, Object>> checkDatabase() {
        return databaseClient
                .sql("SELECT version() as db_version, current_database() as db_name, current_user as db_user")
                .fetch()
                .one()
                .map(row -> Map.of(
                        "status", "connected",
                        "database", row.get("db_name"),
                        "user", row.get("db_user"),
                        "version", row.get("db_version")
                ))
                .onErrorReturn(Map.of(
                        "status", "disconnected",
                        "error", "Could not connect to database"
                ));
    }

}
