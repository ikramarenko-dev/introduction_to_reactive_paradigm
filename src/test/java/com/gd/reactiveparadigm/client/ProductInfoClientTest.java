package com.gd.reactiveparadigm.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

class ProductInfoClientTest {

    private WireMockServer wireMock;
    private ProductInfoClient client;

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMock.start();
        client = new ProductInfoClient(WebClient.builder().baseUrl("http://localhost:" + wireMock.port()).build());
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
    }

    @Test
    void getProductsByCode_returnsProducts() {
        wireMock.stubFor(get(urlPathEqualTo("/productInfoService/product/names"))
                .withQueryParam("productCode", equalTo("3852"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                  {"productId":"111","productCode":"3852","productName":"IceCream","score":3000.0},
                                  {"productId":"222","productCode":"3852","productName":"Milk","score":9000.0}
                                ]
                                """)));

        StepVerifier.create(client.getProductsByCode("3852"))
                .expectNextMatches(products -> products.size() == 2
                        && products.get(1).getProductName().equals("Milk"))
                .verifyComplete();
    }

    @Test
    void getProductsByCode_onTimeout_returnsEmptyList() {
        wireMock.stubFor(get(urlPathEqualTo("/productInfoService/product/names"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("[]")
                        .withFixedDelay(6000)));

        StepVerifier.create(client.getProductsByCode("3852"))
                .expectNextMatches(List::isEmpty)
                .verifyComplete();
    }

    @Test
    void getProductsByCode_onServerError_returnsEmptyList() {
        wireMock.stubFor(get(urlPathEqualTo("/productInfoService/product/names"))
                .willReturn(aResponse().withStatus(500)));

        StepVerifier.create(client.getProductsByCode("3852"))
                .expectNextMatches(List::isEmpty)
                .verifyComplete();
    }
}
