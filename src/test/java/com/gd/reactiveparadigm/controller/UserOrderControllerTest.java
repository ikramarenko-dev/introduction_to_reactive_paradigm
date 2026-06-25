package com.gd.reactiveparadigm.controller;

import com.gd.reactiveparadigm.model.UserOrderResponse;
import com.gd.reactiveparadigm.service.UserOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import static org.mockito.Mockito.when;

@WebFluxTest(UserOrderController.class)
class UserOrderControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private UserOrderService userOrderService;

    @Test
    void getUserOrders_returnsNdjsonStream() {
        UserOrderResponse response = UserOrderResponse.builder()
                .orderNumber("Order_0")
                .userName("John")
                .phoneNumber("123456789")
                .productCode("3852")
                .productName("Milk")
                .productId("222")
                .build();

        when(userOrderService.getOrdersByUserId("user1")).thenReturn(Flux.just(response));

        webTestClient.get()
                .uri("/api/users/user1/orders")
                .header("requestId", "req-test")
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
                .expectBodyList(UserOrderResponse.class)
                .hasSize(1)
                .contains(response);
    }

    @Test
    void getUserOrders_whenServiceErrors_returns500() {
        when(userOrderService.getOrdersByUserId("unknown"))
                .thenReturn(Flux.error(new RuntimeException("User not found: unknown")));

        webTestClient.get()
                .uri("/api/users/unknown/orders")
                .header("requestId", "req-test")
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void getUserOrders_missingRequestIdHeader_returns400() {
        webTestClient.get()
                .uri("/api/users/user1/orders")
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectStatus().isBadRequest();
    }
}
