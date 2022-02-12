package com.task.service_state_poller.service;

import com.task.service_state_poller.dataaccessobject.ExternalServicesRepository;
import com.task.service_state_poller.domainobject.ExternalService;
import com.task.service_state_poller.exception.EntityNotFoundException;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.WriteTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class ExternalServiceServiceImplementation implements ExternalServiceService {
    private Logger logger = LoggerFactory.getLogger(ExternalServiceServiceImplementation.class);
    private final ExternalServicesRepository externalServicesRepository;

    @Value("${request.timeout}")
    private Long timeout;

    @Value("${service.process.concurrency}")
    private int concurrency;

    public ExternalServiceServiceImplementation(ExternalServicesRepository externalServicesRepository) {
        this.externalServicesRepository = externalServicesRepository;
    }

    @Override
    public Flux<ExternalService> findAll() {
        return externalServicesRepository.findAll();
    }

    @Override
    public void monitorExternalServices() {
        Scheduler scheduler = Schedulers.boundedElastic();
        findAll().flatMap(
                externalService -> Mono.defer(() -> {
                    logger.info(String.format("Processing %s on thread %s", externalService, Thread.currentThread().getName()));
                    return sendGetRequest(externalService.getUrl())
                            .map(stringResponseEntity -> processResponse(externalService, stringResponseEntity));
                }).subscribeOn(scheduler)
                , concurrency)
                .subscribe(externalServiceMono -> externalServiceMono.subscribe());
    }

    @Override
    public Mono<ResponseEntity<String>> sendGetRequest(String url) {
        logger.info("Sending Request to url: " + url);
        return WebClient
                .create() //Default Settings
                .get()
                .uri(url)
                .retrieve()
                .toEntity(String.class)
                .timeout(Duration.ofSeconds(timeout))
                .onErrorMap(ReadTimeoutException.class, ex -> new HttpTimeoutException("ReadTimeout"))
                .doOnError(WriteTimeoutException.class, ex -> logger.error("WriteTimeout"))
                .doOnError(Exception.class, ex -> logger.error(ex.getMessage()))
                .onErrorResume(e -> Mono.just("Error " + e.getMessage())
                        .map(s -> ResponseEntity.internalServerError().body(s)));
    }

    public Mono<ExternalService> processResponse(ExternalService externalService, ResponseEntity response) {
        logger.info("Response Received for: " + externalService + " , Status Recieved: " + response.getStatusCode());
        if (response.getStatusCode().is2xxSuccessful()) {
            externalService.setStatus("1");
        } else {
            externalService.setStatus("0");
        }
        return saveStatus(externalService);
    }

    @Transactional
    public Mono<ExternalService> saveStatus(ExternalService externalService) {
        return this.externalServicesRepository.findById(externalService.getId())
                .flatMap(externalServiceDB -> {
                    externalServiceDB.setStatus(externalService.getStatus());
                    externalServiceDB.setLast_verified_datetime(LocalDateTime.now());
                    return this.externalServicesRepository.save(externalServiceDB);
                })
                .onErrorResume(e -> Mono.error(new EntityNotFoundException("Error while updating : " + externalService + ", error: " + e.getMessage())))
                .log("External Service: " + externalService + " updated");

    }
}
