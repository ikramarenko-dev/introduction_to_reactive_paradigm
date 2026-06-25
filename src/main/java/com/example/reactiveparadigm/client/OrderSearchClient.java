package com.example.reactiveparadigm.client;

import com.example.reactiveparadigm.logging.MdcContext;
import com.example.reactiveparadigm.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Slf4j
@Component
public class OrderSearchClient {

    private final WebClient webClient;

    public OrderSearchClient(@Qualifier("orderSearchWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Flux<Order> getOrdersByPhone(String phoneNumber) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/orderSearchService/order/phone")
                        .queryParam("phoneNumber", phoneNumber)
                        .build())
                .accept(MediaType.APPLICATION_NDJSON)
                .retrieve()
                .bodyToFlux(Order.class)
                .doOnEach(signal -> MdcContext.logOnNext(signal, () -> log.info("Received order: {}", signal.get())))
                .doOnEach(signal -> MdcContext.logOnError(signal, () -> log.error("Error fetching orders for phone {}: {}", phoneNumber, signal.getThrowable().getMessage())));
    }
}
