package com.weiyou.common.security.filter;

import com.weiyou.common.security.context.LoginUser;
import com.weiyou.common.security.context.UserContext;
import com.weiyou.common.security.token.TokenPayload;
import com.weiyou.common.security.token.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class BearerTokenAuthenticationFilter extends OncePerRequestFilter {

    private final TokenService tokenService;

    public BearerTokenAuthenticationFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authorization.substring(7);
            TokenPayload payload = tokenService.requireAccessToken(token);
            LoginUser loginUser = new LoginUser(payload.userId(), null, payload.deviceId());
            UserContext.set(loginUser);
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(loginUser, null, List.of())
            );
            filterChain.doFilter(request, response);
        } catch (RuntimeException exception) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"invalid token\",\"traceId\":\"\",\"data\":null}");
        } finally {
            UserContext.clear();
            SecurityContextHolder.clearContext();
        }
    }
}
