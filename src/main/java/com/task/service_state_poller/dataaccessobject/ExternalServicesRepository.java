package com.task.service_state_poller.dataaccessobject;

import com.task.service_state_poller.domainobject.ExternalService;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ExternalServicesRepository extends ReactiveCrudRepository<ExternalService, Integer> {
}
