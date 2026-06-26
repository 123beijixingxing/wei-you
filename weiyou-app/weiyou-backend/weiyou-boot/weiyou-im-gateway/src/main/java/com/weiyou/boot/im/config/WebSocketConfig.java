package com.weiyou.boot.im.config;

import com.weiyou.boot.im.ws.AuthenticatedHandshakeInterceptor;
import com.weiyou.boot.im.ws.ChatWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final AuthenticatedHandshakeInterceptor authenticatedHandshakeInterceptor;
    private final ChatWebSocketHandler chatWebSocketHandler;

    public WebSocketConfig(AuthenticatedHandshakeInterceptor authenticatedHandshakeInterceptor,
                           ChatWebSocketHandler chatWebSocketHandler) {
        this.authenticatedHandshakeInterceptor = authenticatedHandshakeInterceptor;
        this.chatWebSocketHandler = chatWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .addInterceptors(authenticatedHandshakeInterceptor)
                .setAllowedOriginPatterns("*");
    }
}
