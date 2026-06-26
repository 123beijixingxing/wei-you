package com.weiyou.common.core.api;

import java.util.UUID;

public record ApiResponse<T>(int code, String message, String traceId, T data) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(0, "ok", currentTraceId(), data);
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(0, "ok", currentTraceId(), null);
    }

    public static <T> ApiResponse<T> fail(int code, String message) {
        return new ApiResponse<>(code, message, currentTraceId(), null);
    }

    private static String currentTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
