package com.gd.reactiveparadigm.service;

import com.gd.reactiveparadigm.client.OrderSearchClient;
import com.gd.reactiveparadigm.client.ProductInfoClient;
import com.gd.reactiveparadigm.domain.User;
import com.gd.reactiveparadigm.model.Order;
import com.gd.reactiveparadigm.model.Product;
import com.gd.reactiveparadigm.repository.UserInfoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserOrderServiceTest {

    @Mock
    private UserInfoRepository userInfoRepository;
    @Mock
    private OrderSearchClient orderSearchClient;
    @Mock
    private ProductInfoClient productInfoClient;

    @InjectMocks
    private UserOrderService service;

    private final User user = new User("user1", "John", "123456789");

    private final Order order = new Order("123456789", "Order_0", "3852");

    private final List<Product> products = List.of(
            new Product("111", "3852", "IceCream", 3000.0),
            new Product("222", "3852", "Milk", 9000.0),
            new Product("333", "3852", "Meal", 5000.0)
    );

    @Test
    void getOrdersByUserId_returnsMappedResponses() {
        when(userInfoRepository.findById("user1")).thenReturn(Mono.just(user));
        when(orderSearchClient.getOrdersByPhone("123456789")).thenReturn(Flux.just(order));
        when(productInfoClient.getProductsByCode("3852")).thenReturn(Mono.just(products));

        StepVerifier.create(service.getOrdersByUserId("user1"))
                .assertNext(response -> {
                    assertThat(response.getOrderNumber()).isEqualTo("Order_0");
                    assertThat(response.getUserName()).isEqualTo("John");
                    assertThat(response.getPhoneNumber()).isEqualTo("123456789");
                    assertThat(response.getProductCode()).isEqualTo("3852");
                    assertThat(response.getProductName()).isEqualTo("Milk");
                    assertThat(response.getProductId()).isEqualTo("222");
                })
                .verifyComplete();
    }

    @Test
    void getOrdersByUserId_whenUserNotFound_returnsError() {
        when(userInfoRepository.findById("unknown")).thenReturn(Mono.empty());

        StepVerifier.create(service.getOrdersByUserId("unknown"))
                .expectErrorMessage("User not found: unknown")
                .verify();
    }

    @Test
    void getOrdersByUserId_whenProductsEmpty_responseHasNullProductFields() {
        when(userInfoRepository.findById("user1")).thenReturn(Mono.just(user));
        when(orderSearchClient.getOrdersByPhone("123456789")).thenReturn(Flux.just(order));
        when(productInfoClient.getProductsByCode("3852")).thenReturn(Mono.just(Collections.emptyList()));

        StepVerifier.create(service.getOrdersByUserId("user1"))
                .assertNext(response -> {
                    assertThat(response.getProductName()).isNull();
                    assertThat(response.getProductId()).isNull();
                })
                .verifyComplete();
    }

    @Test
    void getOrdersByUserId_selectsBestProductByScore() {
        when(userInfoRepository.findById("user1")).thenReturn(Mono.just(user));
        when(orderSearchClient.getOrdersByPhone("123456789")).thenReturn(Flux.just(order));
        when(productInfoClient.getProductsByCode("3852")).thenReturn(Mono.just(products));

        StepVerifier.create(service.getOrdersByUserId("user1"))
                .assertNext(response -> assertThat(response.getProductName()).isEqualTo("Milk"))
                .verifyComplete();
    }

    @Test
    void getOrdersByUserId_streamsMultipleOrders() {
        Order order2 = new Order("123456789", "Order_1", "5256");
        List<Product> products2 = List.of(new Product("111", "5256", "Apple", 7000.0));

        when(userInfoRepository.findById("user1")).thenReturn(Mono.just(user));
        when(orderSearchClient.getOrdersByPhone("123456789")).thenReturn(Flux.just(order, order2));
        when(productInfoClient.getProductsByCode("3852")).thenReturn(Mono.just(products));
        when(productInfoClient.getProductsByCode("5256")).thenReturn(Mono.just(products2));

        StepVerifier.create(service.getOrdersByUserId("user1"))
                .expectNextCount(2)
                .verifyComplete();
    }
}
