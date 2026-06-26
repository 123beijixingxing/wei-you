package com.weiyou.common.security.token;

import com.weiyou.common.core.exception.BusinessException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final String secret;
    private final long accessExpireSeconds;
    private final long refreshExpireSeconds;

    public TokenService(@Value("${weiyou.security.token-secret:weiyou-dev-secret}") String secret,
                        @Value("${weiyou.security.access-expire-seconds:7200}") long accessExpireSeconds,
                        @Value("${weiyou.security.refresh-expire-seconds:2592000}") long refreshExpireSeconds) {
        this.secret = secret;
        this.accessExpireSeconds = accessExpireSeconds;
        this.refreshExpireSeconds = refreshExpireSeconds;
    }

    public String createAccessToken(Long userId, String deviceId) {
        return createToken(userId, deviceId, "access", accessExpireSeconds);
    }

    public String createRefreshToken(Long userId, String deviceId) {
        return createToken(userId, deviceId, "refresh", refreshExpireSeconds);
    }

    public TokenPayload requireAccessToken(String token) {
        return requireToken(token, "access");
    }

    public TokenPayload requireRefreshToken(String token) {
        return requireToken(token, "refresh");
    }

    public Optional<TokenPayload> parse(String token) {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = decoded.split("\\|", -1);
            if (parts.length != 5) {
                return Optional.empty();
            }
            String payload = String.join("|", parts[0], parts[1], parts[2], parts[3]);
            String signature = parts[4];
            if (!signature.equals(sign(payload))) {
                return Optional.empty();
            }
            long expireAt = Long.parseLong(parts[2]);
            if (System.currentTimeMillis() > expireAt) {
                return Optional.empty();
            }
            return Optional.of(new TokenPayload(Long.parseLong(parts[0]), parts[1], expireAt, parts[3]));
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    private TokenPayload requireToken(String token, String expectedType) {
        Optional<TokenPayload> optional = parse(token);
        if (optional.isEmpty()) {
            throw new BusinessException(401, "invalid token");
        }
        TokenPayload payload = optional.get();
        if (!expectedType.equals(payload.tokenType())) {
            throw new BusinessException(401, "invalid token type");
        }
        return payload;
    }

    private String createToken(Long userId, String deviceId, String tokenType, long expireSeconds) {
        long expireAt = System.currentTimeMillis() + (expireSeconds * 1000);
        String payload = userId + "|" + deviceId + "|" + expireAt + "|" + tokenType;
        String token = payload + "|" + sign(payload);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token.getBytes(StandardCharsets.UTF_8));
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] signBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signBytes);
        } catch (Exception exception) {
            throw new IllegalStateException("failed to sign token", exception);
        }
    }
}
