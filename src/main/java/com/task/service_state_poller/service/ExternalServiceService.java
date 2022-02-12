package com.task.service_state_poller.service;

import com.task.service_state_poller.domainobject.ExternalService;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ExternalServiceService {
    Flux<ExternalService> findAll();
    void monitorExternalServices();
    Mono<ResponseEntity<String>> sendGetRequest(String url);
    Mono<ExternalService> processResponse(ExternalService externalService, ResponseEntity response);
}
