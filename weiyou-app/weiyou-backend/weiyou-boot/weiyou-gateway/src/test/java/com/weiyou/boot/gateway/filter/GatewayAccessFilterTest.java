package com.weiyou.boot.gateway.filter;

import com.weiyou.boot.gateway.config.GatewayPathProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GatewayAccessFilterTest {

    @Test
    void shouldAllowPublicPathWithoutAuthorization() {
        GatewayPathProperties properties = new GatewayPathProperties();
        properties.setPublicPaths(java.util.List.of("/api/auth/login/password"));
        GatewayAccessFilter filter = new GatewayAccessFilter(properties);

        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/auth/login/password").build()
        );

        filter.filter(exchange, chain).block();

        verify(chain).filter(any());
        Assertions.assertNotNull(exchange.getResponse().getHeaders().getFirst("X-Trace-Id"));
    }

    @Test
    void shouldRejectProtectedApiWithoutBearerToken() {
        GatewayPathProperties properties = new GatewayPathProperties();
        properties.setPublicPaths(java.util.List.of("/api/auth/login/password"));
        GatewayAccessFilter filter = new GatewayAccessFilter(properties);

        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/user/profile/me").build()
        );

        filter.filter(exchange, chain).block();

        Assertions.assertEquals(401, exchange.getResponse().getStatusCode().value());
        Assertions.assertTrue(exchange.getResponse().getBodyAsString().block().contains("missing bearer token"));
        verify(chain, never()).filter(any());
    }

    @Test
    void shouldRejectWebSocketWithoutToken() {
        GatewayPathProperties properties = new GatewayPathProperties();
        GatewayAccessFilter filter = new GatewayAccessFilter(properties);

        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/ws/chat?deviceId=test-device").build()
        );

        filter.filter(exchange, chain).block();

        Assertions.assertEquals(401, exchange.getResponse().getStatusCode().value());
        Assertions.assertTrue(exchange.getResponse().getBodyAsString().block().contains("missing websocket token"));
        verify(chain, never()).filter(any());
    }

    @Test
    void shouldWrapDownstreamExceptionAsBadGateway() {
        GatewayPathProperties properties = new GatewayPathProperties();
        GatewayAccessFilter filter = new GatewayAccessFilter(properties);

        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.error(new IllegalStateException("boom")));

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/user/profile/me")
                        .header("Authorization", "Bearer token-demo")
                        .build()
        );

        filter.filter(exchange, chain).block();

        Assertions.assertEquals(502, exchange.getResponse().getStatusCode().value());
        Assertions.assertTrue(exchange.getResponse().getBodyAsString().block().contains("gateway downstream error"));
    }
}
