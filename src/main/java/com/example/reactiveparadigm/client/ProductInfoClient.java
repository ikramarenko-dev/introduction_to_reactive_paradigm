package com.example.reactiveparadigm.client;

import com.example.reactiveparadigm.logging.MdcContext;
import com.example.reactiveparadigm.model.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class ProductInfoClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final WebClient webClient;

    public ProductInfoClient(@Qualifier("productInfoWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<List<Product>> getProductsByCode(String productCode) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/productInfoService/product/names")
                        .queryParam("productCode", productCode)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Product>>() {})
                .timeout(TIMEOUT)
                .doOnEach(signal -> MdcContext.logOnNext(signal, () -> log.info("Received products for code {}: {}", productCode, signal.get())))
                .doOnEach(signal -> MdcContext.logOnError(signal, () -> log.error("Error fetching products for code {}: {}", productCode, signal.getThrowable().getMessage())))
                .onErrorReturn(Collections.emptyList());
    }
}
