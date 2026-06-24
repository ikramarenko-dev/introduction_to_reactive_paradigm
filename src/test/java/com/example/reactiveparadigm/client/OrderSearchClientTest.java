package com.example.reactiveparadigm.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

class OrderSearchClientTest {

    private WireMockServer wireMock;
    private OrderSearchClient client;

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMock.start();
        client = new OrderSearchClient(WebClient.builder().baseUrl("http://localhost:" + wireMock.port()).build());
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
    }

    @Test
    void getOrdersByPhone_returnsOrders() {
        wireMock.stubFor(get(urlPathEqualTo("/orderSearchService/order/phone"))
                .withQueryParam("phoneNumber", equalTo("123456789"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/x-ndjson")
                        .withBody("""
                                {"phoneNumber":"123456789","orderNumber":"Order_0","productCode":"3852"}
                                {"phoneNumber":"123456789","orderNumber":"Order_1","productCode":"5256"}
                                """)));

        StepVerifier.create(client.getOrdersByPhone("123456789"))
                .expectNextMatches(o -> o.getOrderNumber().equals("Order_0") && o.getProductCode().equals("3852"))
                .expectNextMatches(o -> o.getOrderNumber().equals("Order_1") && o.getProductCode().equals("5256"))
                .verifyComplete();
    }

    @Test
    void getOrdersByPhone_onServerError_propagatesError() {
        wireMock.stubFor(get(urlPathEqualTo("/orderSearchService/order/phone"))
                .willReturn(aResponse().withStatus(500)));

        StepVerifier.create(client.getOrdersByPhone("123456789"))
                .expectError()
                .verify();
    }
}
