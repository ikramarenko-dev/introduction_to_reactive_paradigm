package com.example.reactiveparadigm.service;

import com.example.reactiveparadigm.client.OrderSearchClient;
import com.example.reactiveparadigm.client.ProductInfoClient;
import com.example.reactiveparadigm.domain.User;
import com.example.reactiveparadigm.model.Order;
import com.example.reactiveparadigm.model.Product;
import com.example.reactiveparadigm.model.UserOrderResponse;
import com.example.reactiveparadigm.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserOrderService {

    private final UserInfoRepository userInfoRepository;
    private final OrderSearchClient orderSearchClient;
    private final ProductInfoClient productInfoClient;

    public Flux<UserOrderResponse> getOrdersByUserId(String userId) {
        return userInfoRepository.findById(userId)
                .switchIfEmpty(Mono.error(new RuntimeException("User not found: " + userId)))
                .flatMapMany(this::fetchOrdersForUser);
    }

    private Flux<UserOrderResponse> fetchOrdersForUser(User user) {
        return orderSearchClient.getOrdersByPhone(user.getPhoneNumber())
                .flatMap(order -> buildResponse(order, user));
    }

    private Mono<UserOrderResponse> buildResponse(Order order, User user) {
        return productInfoClient.getProductsByCode(order.getProductCode())
                .map(products -> {
                    Product bestProduct = selectBestProduct(products);
                    return UserOrderResponse.builder()
                            .orderNumber(order.getOrderNumber())
                            .userName(user.getUserName())
                            .phoneNumber(user.getPhoneNumber())
                            .productCode(order.getProductCode())
                            .productName(bestProduct != null ? bestProduct.getProductName() : null)
                            .productId(bestProduct != null ? bestProduct.getProductId() : null)
                            .build();
                });
    }

    private Product selectBestProduct(List<Product> products) {
        if (products == null || products.isEmpty()) return null;
        return products.stream()
                .max(Comparator.comparingDouble(Product::getScore))
                .orElse(null);
    }
}
