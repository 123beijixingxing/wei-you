package com.weiyou.boot.im.ws;

import com.weiyou.common.security.context.LoginUser;
import com.weiyou.common.security.token.TokenPayload;
import com.weiyou.common.security.token.TokenService;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Component
public class AuthenticatedHandshakeInterceptor implements HandshakeInterceptor {

    public static final String ATTR_LOGIN_USER = "loginUser";

    private final TokenService tokenService;

    public AuthenticatedHandshakeInterceptor(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String token = queryValue(request.getURI(), "token").orElse(null);
        if (token == null || token.isBlank()) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
        try {
            TokenPayload payload = tokenService.requireAccessToken(token);
            String deviceId = queryValue(request.getURI(), "deviceId").orElse(payload.deviceId());
            attributes.put(ATTR_LOGIN_USER, new LoginUser(payload.userId(), null, deviceId));
            return true;
        } catch (RuntimeException exception) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }

    private Optional<String> queryValue(URI uri, String key) {
        String query = uri.getQuery();
        if (query == null || query.isBlank()) {
            return Optional.empty();
        }
        return Arrays.stream(query.split("&"))
                .map(item -> item.split("=", 2))
                .filter(parts -> parts.length == 2 && key.equals(parts[0]))
                .map(parts -> parts[1])
                .findFirst();
    }
}
