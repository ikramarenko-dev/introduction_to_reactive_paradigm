package com.gd.reactiveparadigm.controller;

import com.gd.reactiveparadigm.model.UserOrderResponse;
import com.gd.reactiveparadigm.service.UserOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static com.gd.reactiveparadigm.logging.MdcContext.withRequestId;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserOrderController {

    private final UserOrderService userOrderService;

    @GetMapping(value = "/{userId}/orders", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<UserOrderResponse> getUserOrders(
            @PathVariable String userId,
            @RequestHeader("requestId") String requestId) {

        return userOrderService.getOrdersByUserId(userId)
                .contextWrite(withRequestId(requestId));
    }
}
