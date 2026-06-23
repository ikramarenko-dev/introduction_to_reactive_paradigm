package com.example.reactiveparadigm.logging;

import org.slf4j.MDC;
import reactor.core.publisher.Signal;
import reactor.util.context.Context;

import java.util.function.Consumer;

public final class MdcContext {

    public static final String REQUEST_ID_KEY = "requestId";

    private MdcContext() {}

    public static Context withRequestId(String requestId) {
        return Context.of(REQUEST_ID_KEY, requestId);
    }

    public static <T> Consumer<Signal<T>> logOnNext(Consumer<T> logStatement) {
        return signal -> {
            if (!signal.isOnNext()) return;
            String requestId = signal.getContextView().getOrDefault(REQUEST_ID_KEY, "");
            try (MDC.MDCCloseable ignored = MDC.putCloseable(REQUEST_ID_KEY, requestId)) {
                logStatement.accept(signal.get());
            }
        };
    }

    public static <T> Consumer<Signal<T>> logOnError(Consumer<Throwable> logStatement) {
        return signal -> {
            if (!signal.isOnError()) return;
            String requestId = signal.getContextView().getOrDefault(REQUEST_ID_KEY, "");
            try (MDC.MDCCloseable ignored = MDC.putCloseable(REQUEST_ID_KEY, requestId)) {
                logStatement.accept(signal.getThrowable());
            }
        };
    }
}
