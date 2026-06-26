package com.weiyou.common.security.token;

import com.weiyou.common.core.exception.BusinessException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TokenServiceTest {

    @Test
    void shouldCreateAndParseAccessToken() {
        TokenService tokenService = new TokenService("test-secret", 3600, 7200);

        String token = tokenService.createAccessToken(10001L, "device-a");
        TokenPayload payload = tokenService.requireAccessToken(token);

        Assertions.assertEquals(10001L, payload.userId());
        Assertions.assertEquals("device-a", payload.deviceId());
        Assertions.assertEquals("access", payload.tokenType());
    }

    @Test
    void shouldRejectRefreshTokenWhenUsingAccessParser() {
        TokenService tokenService = new TokenService("test-secret", 3600, 7200);

        String token = tokenService.createRefreshToken(10001L, "device-a");

        BusinessException exception = Assertions.assertThrows(BusinessException.class,
                () -> tokenService.requireAccessToken(token));
        Assertions.assertEquals(401, exception.getCode());
    }

    @Test
    void shouldRejectExpiredToken() {
        TokenService tokenService = new TokenService("test-secret", -1, 7200);

        String token = tokenService.createAccessToken(10001L, "device-a");

        BusinessException exception = Assertions.assertThrows(BusinessException.class,
                () -> tokenService.requireAccessToken(token));
        Assertions.assertEquals(401, exception.getCode());
    }
}
