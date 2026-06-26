package com.weiyou.boot.gateway.filter;

import com.weiyou.boot.gateway.config.GatewayPathProperties;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GatewayRateLimitFilterTest {

    @Test
    void shouldUseUserIdIdentityForWalletRule() {
        GatewayPathProperties properties = new GatewayPathProperties();
        GatewayPathProperties.RateLimit rateLimit = properties.getRateLimit();
        GatewayPathProperties.Rule rule = new GatewayPathProperties.Rule();
        rule.setPath("/api/wallet/**");
        rule.setIdentityMode(GatewayPathProperties.IdentityMode.USER_ID);
        rule.setMaxRequests(10);
        rule.setWindowSeconds(60);
        rateLimit.setRules(java.util.List.of(rule));

        ReactiveStringRedisTemplate redisTemplate = mock(ReactiveStringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ReactiveValueOperations<String, String> valueOperations = mock(ReactiveValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        AtomicReference<String> keyRef = new AtomicReference<>();
        when(valueOperations.increment(anyString())).thenAnswer(invocation -> {
            keyRef.set(invocation.getArgument(0));
            return Mono.just(1L);
        });
        when(redisTemplate.expire(anyString(), any())).thenReturn(Mono.just(true));

        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        String token = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("10001|device-a|9999999999999|access|sig".getBytes(StandardCharsets.UTF_8));
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/wallet/overview")
                .header("Authorization", "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayRateLimitFilter filter = new GatewayRateLimitFilter(properties, redisTemplate);
        filter.filter(exchange, chain).block();

        Assertions.assertNotNull(keyRef.get());
        Assertions.assertTrue(keyRef.get().contains("user:10001"));
    }

    @Test
    void shouldUseIpIdentityForLoginRule() {
        GatewayPathProperties properties = new GatewayPathProperties();
        GatewayPathProperties.RateLimit rateLimit = properties.getRateLimit();
        GatewayPathProperties.Rule rule = new GatewayPathProperties.Rule();
        rule.setPath("/api/auth/login/password");
        rule.setIdentityMode(GatewayPathProperties.IdentityMode.IP);
        rule.setMaxRequests(10);
        rule.setWindowSeconds(60);
        rateLimit.setRules(java.util.List.of(rule));

        ReactiveStringRedisTemplate redisTemplate = mock(ReactiveStringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ReactiveValueOperations<String, String> valueOperations = mock(ReactiveValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        AtomicReference<String> keyRef = new AtomicReference<>();
        when(valueOperations.increment(anyString())).thenAnswer(invocation -> {
            keyRef.set(invocation.getArgument(0));
            return Mono.just(1L);
        });
        when(redisTemplate.expire(anyString(), any())).thenReturn(Mono.just(true));

        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        MockServerHttpRequest request = MockServerHttpRequest.post("/api/auth/login/password")
                .header("X-Real-IP", "10.10.10.8")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayRateLimitFilter filter = new GatewayRateLimitFilter(properties, redisTemplate);
        filter.filter(exchange, chain).block();

        Assertions.assertNotNull(keyRef.get());
        Assertions.assertTrue(keyRef.get().contains("ip:10.10.10.8"));
    }

    @Test
    void shouldReturnTooManyRequestsWhenLimitExceeded() {
        GatewayPathProperties properties = new GatewayPathProperties();
        properties.getRateLimit().setMaxRequests(1);
        properties.getRateLimit().setWindowSeconds(60);
        properties.getRateLimit().setIncludePaths(java.util.List.of("/api/**"));
        properties.getRateLimit().setExcludePaths(java.util.List.of());

        ReactiveStringRedisTemplate redisTemplate = mock(ReactiveStringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ReactiveValueOperations<String, String> valueOperations = mock(ReactiveValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(Mono.just(2L));
        when(redisTemplate.expire(anyString(), any())).thenReturn(Mono.just(true));

        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/chat/conversation/list").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayRateLimitFilter filter = new GatewayRateLimitFilter(properties, redisTemplate);
        filter.filter(exchange, chain).block();

        Assertions.assertEquals(429, exchange.getResponse().getStatusCode().value());
        String body = exchange.getResponse().getBodyAsString().block();
        Assertions.assertTrue(body.contains("too many requests"));
        verify(chain, never()).filter(any());
    }
}
