package com.weiyou.auth.controller;

import com.weiyou.auth.app.service.AuthPersistenceService;
import com.weiyou.auth.domain.entity.UserAccountEntity;
import com.weiyou.auth.domain.entity.UserDeviceEntity;
import com.weiyou.common.security.context.LoginUser;
import com.weiyou.common.security.context.UserContext;
import com.weiyou.common.security.token.TokenPayload;
import com.weiyou.common.security.token.TokenService;
import com.weiyou.common.web.exception.GlobalExceptionHandler;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {

    private AuthPersistenceService authPersistenceService;
    private TokenService tokenService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        authPersistenceService = mock(AuthPersistenceService.class);
        tokenService = mock(TokenService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authPersistenceService, tokenService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void shouldLoginByPassword() throws Exception {
        UserAccountEntity account = new UserAccountEntity();
        account.setUserId(10001L);

        when(authPersistenceService.authenticateByPassword("13800000001", "123456"))
                .thenReturn(Optional.of(account));
        when(tokenService.createAccessToken(10001L, "device-demo-1")).thenReturn("access-token-demo");
        when(tokenService.createRefreshToken(10001L, "device-demo-1")).thenReturn("refresh-token-demo");

        mockMvc.perform(post("/auth/login/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "mobile": "13800000001",
                                  "password": "123456",
                                  "deviceId": "device-demo-1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.userId").value(10001))
                .andExpect(jsonPath("$.data.accessToken").value("access-token-demo"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token-demo"));

        verify(authPersistenceService).touchLoginDevice(10001L, "device-demo-1", "app");
    }

    @Test
    void shouldRefreshToken() throws Exception {
        when(tokenService.requireRefreshToken("refresh-token-demo"))
                .thenReturn(new TokenPayload(10001L, "device-demo-1", System.currentTimeMillis() + 3600000L, "refresh"));
        when(tokenService.createAccessToken(10001L, "device-demo-1")).thenReturn("new-access-token");
        when(tokenService.createRefreshToken(10001L, "device-demo-1")).thenReturn("new-refresh-token");

        mockMvc.perform(post("/auth/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "refresh-token-demo"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.userId").value(10001))
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"));
    }

    @Test
    void shouldListDevicesForCurrentUser() throws Exception {
        UserContext.set(new LoginUser(10001L, "tester", "device-demo-1"));

        UserDeviceEntity device = new UserDeviceEntity();
        device.setDeviceId("device-demo-1");
        device.setDeviceType("android");
        device.setDeviceModel("Xiaomi 14");
        device.setSystemVersion("Android 15");
        device.setLoginCity("Shenzhen");
        device.setLoginIp("127.0.0.1");
        device.setLastLoginAt(LocalDateTime.of(2026, 5, 11, 9, 0));
        device.setOnlineStatus(1);

        when(authPersistenceService.listUserDevices(10001L)).thenReturn(List.of(device));

        mockMvc.perform(get("/auth/device/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].deviceId").value("device-demo-1"))
                .andExpect(jsonPath("$.data[0].online").value(true));
    }
}
