package com.weiyou.boot.gateway.filter;

import com.weiyou.boot.gateway.config.GatewayPathProperties;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class GatewayAccessFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(GatewayAccessFilter.class);
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final String TRACE_HEADER = "X-Trace-Id";

    private final GatewayPathProperties gatewayPathProperties;

    public GatewayAccessFilter(GatewayPathProperties gatewayPathProperties) {
        this.gatewayPathProperties = gatewayPathProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String traceId = resolveTraceId(exchange.getRequest().getHeaders().getFirst(TRACE_HEADER));
        long startAt = System.currentTimeMillis();
        String path = exchange.getRequest().getURI().getPath();

        ServerWebExchange tracedExchange = exchange.mutate()
                .request(builder -> builder.header(TRACE_HEADER, traceId))
                .build();
        tracedExchange.getResponse().getHeaders().set(TRACE_HEADER, traceId);

        if (requiresHttpAuth(path) && !hasBearerToken(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))) {
            return writeJsonError(tracedExchange, HttpStatus.UNAUTHORIZED, traceId, "missing bearer token");
        }
        if (requiresWsAuth(path) && !hasWsToken(path, exchange.getRequest().getURI().getQuery())) {
            return writeJsonError(tracedExchange, HttpStatus.UNAUTHORIZED, traceId, "missing websocket token");
        }

        return chain.filter(tracedExchange)
                .onErrorResume(throwable -> {
                    log.error("gateway downstream error path={} traceId={}", path, traceId, throwable);
                    return writeJsonError(tracedExchange, HttpStatus.BAD_GATEWAY, traceId, "gateway downstream error");
                })
                .doFinally(signalType -> {
                    int status = tracedExchange.getResponse().getStatusCode() == null
                            ? 200
                            : tracedExchange.getResponse().getStatusCode().value();
                    long cost = System.currentTimeMillis() - startAt;
                    log.info("gateway {} {} -> {} ({} ms) traceId={}",
                            tracedExchange.getRequest().getMethod(), path, status, cost, traceId);
                });
    }

    @Override
    public int getOrder() {
        return -200;
    }

    private boolean requiresHttpAuth(String path) {
        if (!path.startsWith("/api/")) {
            return false;
        }
        return gatewayPathProperties.getPublicPaths().stream().noneMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }

    private boolean requiresWsAuth(String path) {
        return path.startsWith("/ws/");
    }

    private boolean hasBearerToken(String authorization) {
        return authorization != null && authorization.startsWith("Bearer ") && authorization.length() > 7;
    }

    private boolean hasWsToken(String path, String query) {
        if (!path.startsWith("/ws/chat")) {
            return true;
        }
        return query != null && query.contains("token=");
    }

    private String resolveTraceId(String traceId) {
        if (traceId == null || traceId.isBlank()) {
            return UUID.randomUUID().toString().replace("-", "");
        }
        return traceId;
    }

    private Mono<Void> writeJsonError(ServerWebExchange exchange, HttpStatus status, String traceId, String message) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String payload = "{\"code\":" + status.value() + ",\"message\":\"" + message + "\",\"traceId\":\"" + traceId + "\",\"data\":null}";
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(payload.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
