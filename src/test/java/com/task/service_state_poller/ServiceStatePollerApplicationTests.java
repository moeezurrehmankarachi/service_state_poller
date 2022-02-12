package com.task.service_state_poller;

import com.task.service_state_poller.domainobject.ExternalService;
import com.task.service_state_poller.service.ExternalServiceService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ServiceStatePollerApplicationTests {
	@Autowired
	private ExternalServiceService externalServiceService;

	@ParameterizedTest
	@MethodSource("sendGetRequestTestParameters")
	void sendGetRequestTest(ExternalService externalService, int expectedResponse) {
		ResponseEntity responseEntity = externalServiceService.sendGetRequest(externalService.getUrl()).block();
		System.out.println(responseEntity.getStatusCode().value());
		assertEquals(expectedResponse, responseEntity.getStatusCode().value());
	}

	private static Stream sendGetRequestTestParameters() {
		return Stream.of(
				Arguments.of(new ExternalService(1, "Google","https://www.google.com",  "1"), HttpStatus.OK.value()),
				Arguments.of(new ExternalService(1, "Service4","",  "1"), HttpStatus.INTERNAL_SERVER_ERROR.value()),
				Arguments.of(new ExternalService(1, "Service5",null,  "1"), HttpStatus.INTERNAL_SERVER_ERROR.value()),
//				Arguments.of(new ExternalService(1, "bing","https://www.bing.com", "1"), HttpStatus.OK.value()),
				Arguments.of(new ExternalService(1, "facebook","www.facebook.com",  "1"), HttpStatus.MOVED_PERMANENTLY.value()),
				Arguments.of(new ExternalService(1, "example","http://192.168.1.113:8088",  "1"), HttpStatus.INTERNAL_SERVER_ERROR.value())

		);
	}

	@ParameterizedTest
	@MethodSource("processResponseParameters")
	void processResponseTest(ExternalService externalService, ResponseEntity response, String expectedResponse) {
		externalServiceService.processResponse(externalService, response);
		System.out.println(externalService + "\n");
		assertEquals(expectedResponse, externalService.getStatus());
	}

	private static Stream processResponseParameters() {
		return Stream.of(
				Arguments.of(new ExternalService(1, "bing","www.bing.com", "1"), ResponseEntity.status(HttpStatus.OK).build(), "1"),
				Arguments.of(new ExternalService(1, "facebook","www.facebook.com",  "1"), ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY).build(), "0"),
				Arguments.of(new ExternalService(1, "example","http://192.168.1.113:8088",  "1"), ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(), "0")
		);
	}
}
