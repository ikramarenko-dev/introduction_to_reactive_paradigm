package com.gd.reactiveparadigm.logging;

import org.slf4j.MDC;
import reactor.core.publisher.Signal;
import reactor.util.context.Context;

public final class MdcContext {

    public static final String REQUEST_ID_KEY = "requestId";

    private MdcContext() {}

    public static Context withRequestId(String requestId) {
        return Context.of(REQUEST_ID_KEY, requestId);
    }

    public static void logOnNext(Signal<?> signal, Runnable logStatement) {
        if (!signal.isOnNext()) return;
        String requestId = signal.getContextView().getOrDefault(REQUEST_ID_KEY, "");
        try (MDC.MDCCloseable ignored = MDC.putCloseable(REQUEST_ID_KEY, requestId)) {
            logStatement.run();
        }
    }

    public static void logOnError(Signal<?> signal, Runnable logStatement) {
        if (!signal.isOnError()) return;
        String requestId = signal.getContextView().getOrDefault(REQUEST_ID_KEY, "");
        try (MDC.MDCCloseable ignored = MDC.putCloseable(REQUEST_ID_KEY, requestId)) {
            logStatement.run();
        }
    }
}
