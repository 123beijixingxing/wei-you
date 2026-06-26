package com.weiyou.boot.gateway.filter;

import com.weiyou.boot.gateway.config.GatewayPathProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class GatewayRateLimitFilter implements GlobalFilter, Ordered {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final String TRACE_HEADER = "X-Trace-Id";
    private static final String RATE_KEY_PREFIX = "wy:gateway:rate:";

    private final GatewayPathProperties gatewayPathProperties;
    private final ReactiveStringRedisTemplate reactiveStringRedisTemplate;

    public GatewayRateLimitFilter(GatewayPathProperties gatewayPathProperties,
                                  ReactiveStringRedisTemplate reactiveStringRedisTemplate) {
        this.gatewayPathProperties = gatewayPathProperties;
        this.reactiveStringRedisTemplate = reactiveStringRedisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        GatewayPathProperties.RateLimit rateLimit = gatewayPathProperties.getRateLimit();
        if (!shouldLimit(path, rateLimit)) {
            return chain.filter(exchange);
        }

        GatewayPathProperties.Rule matchedRule = matchRule(path, rateLimit);
        int maxRequests = matchedRule != null && matchedRule.getMaxRequests() != null
                ? matchedRule.getMaxRequests()
                : rateLimit.getMaxRequests();
        int windowSeconds = matchedRule != null && matchedRule.getWindowSeconds() != null
                ? matchedRule.getWindowSeconds()
                : rateLimit.getWindowSeconds();
        GatewayPathProperties.IdentityMode identityMode = matchedRule != null && matchedRule.getIdentityMode() != null
                ? matchedRule.getIdentityMode()
                : rateLimit.getIdentityMode();

        String clientId = resolveRateLimitIdentity(exchange, identityMode);
        long window = Instant.now().getEpochSecond() / Math.max(windowSeconds, 1);
        String redisKey = RATE_KEY_PREFIX + clientId + ":" + path.replace('/', '_') + ":" + window;
        Duration ttl = Duration.ofSeconds(Math.max(windowSeconds, 1));

        return reactiveStringRedisTemplate.opsForValue().increment(redisKey)
                .flatMap(count -> {
                    Mono<Boolean> expireMono = count == 1
                            ? reactiveStringRedisTemplate.expire(redisKey, ttl)
                            : Mono.just(Boolean.TRUE);
                    return expireMono.thenReturn(count);
                })
                .flatMap(count -> {
                    if (count != null && count > maxRequests) {
                        String traceId = resolveTraceId(exchange.getRequest().getHeaders().getFirst(TRACE_HEADER));
                        exchange.getResponse().getHeaders().set(TRACE_HEADER, traceId);
                        return writeJsonError(exchange, HttpStatus.TOO_MANY_REQUESTS, traceId, "too many requests");
                    }
                    return chain.filter(exchange);
                })
                .onErrorResume(throwable -> {
                    String traceId = resolveTraceId(exchange.getRequest().getHeaders().getFirst(TRACE_HEADER));
                    exchange.getResponse().getHeaders().set(TRACE_HEADER, traceId);
                    return writeJsonError(exchange, HttpStatus.SERVICE_UNAVAILABLE, traceId, "gateway rate limiter unavailable");
                });
    }

    @Override
    public int getOrder() {
        return -300;
    }

    private boolean shouldLimit(String path, GatewayPathProperties.RateLimit rateLimit) {
        if (!rateLimit.isEnabled()) {
            return false;
        }
        boolean included = rateLimit.getIncludePaths().stream().anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
        boolean excluded = rateLimit.getExcludePaths().stream().anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
        return included && !excluded;
    }

    private GatewayPathProperties.Rule matchRule(String path, GatewayPathProperties.RateLimit rateLimit) {
        return rateLimit.getRules().stream()
                .filter(rule -> rule.getPath() != null && PATH_MATCHER.match(rule.getPath(), path))
                .findFirst()
                .orElse(null);
    }

    private String resolveRateLimitIdentity(ServerWebExchange exchange, GatewayPathProperties.IdentityMode identityMode) {
        return switch (identityMode) {
            case IP -> "ip:" + resolveClientIp(exchange);
            case TOKEN -> resolveTokenIdentity(exchange).orElse("ip:" + resolveClientIp(exchange));
            case USER_ID -> resolveUserIdentity(exchange).orElseGet(() -> resolveTokenIdentity(exchange).orElse("ip:" + resolveClientIp(exchange)));
            case AUTO -> resolveUserIdentity(exchange)
                    .orElseGet(() -> resolveTokenIdentity(exchange).orElse("ip:" + resolveClientIp(exchange)));
        };
    }

    private java.util.Optional<String> resolveUserIdentity(ServerWebExchange exchange) {
        return extractToken(exchange)
                .flatMap(this::extractUserId)
                .map(userId -> "user:" + userId);
    }

    private java.util.Optional<String> resolveTokenIdentity(ServerWebExchange exchange) {
        return extractToken(exchange).map(token -> "token:" + sha256(token));
    }

    private java.util.Optional<String> extractToken(ServerWebExchange exchange) {
        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization != null && authorization.startsWith("Bearer ") && authorization.length() > 7) {
            return java.util.Optional.of(authorization.substring(7));
        }
        String query = exchange.getRequest().getURI().getQuery();
        if (query == null || query.isBlank()) {
            return java.util.Optional.empty();
        }
        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2 && Objects.equals(parts[0], "token") && !parts[1].isBlank()) {
                return java.util.Optional.of(parts[1]);
            }
        }
        return java.util.Optional.empty();
    }

    private java.util.Optional<String> extractUserId(String token) {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = decoded.split("\\|", -1);
            if (parts.length < 1 || parts[0].isBlank()) {
                return java.util.Optional.empty();
            }
            Long.parseLong(parts[0]);
            return java.util.Optional.of(parts[0]);
        } catch (Exception exception) {
            return java.util.Optional.empty();
        }
    }

    private String sha256(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte item : bytes) {
                builder.append(String.format("%02x", item));
            }
            return builder.toString();
        } catch (Exception exception) {
            return UUID.randomUUID().toString().replace("-", "");
        }
    }

    private String resolveClientIp(ServerWebExchange exchange) {
        String forwarded = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp;
        }
        if (exchange.getRequest().getRemoteAddress() != null && exchange.getRequest().getRemoteAddress().getAddress() != null) {
            return exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        }
        return UUID.randomUUID().toString().replace("-", "");
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
