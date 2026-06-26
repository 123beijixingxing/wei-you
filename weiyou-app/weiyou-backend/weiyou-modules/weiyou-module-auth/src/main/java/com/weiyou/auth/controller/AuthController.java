package com.weiyou.auth.controller;

import com.weiyou.auth.app.service.AuthPersistenceService;
import com.weiyou.auth.domain.entity.UserAccountEntity;
import com.weiyou.auth.domain.entity.UserDeviceEntity;
import com.weiyou.common.core.api.ApiResponse;
import com.weiyou.common.core.exception.BusinessException;
import com.weiyou.common.security.context.UserContext;
import com.weiyou.common.security.token.TokenPayload;
import com.weiyou.common.security.token.TokenService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthPersistenceService authPersistenceService;
    private final TokenService tokenService;

    public AuthController(AuthPersistenceService authPersistenceService, TokenService tokenService) {
        this.authPersistenceService = authPersistenceService;
        this.tokenService = tokenService;
    }

    @PostMapping("/login/password")
    public ApiResponse<LoginTokenData> loginByPassword(@Valid @RequestBody PasswordLoginRequest request) {
        UserAccountEntity account = authPersistenceService.authenticateByPassword(request.mobile(), request.password())
                .orElseThrow(() -> new BusinessException(401, "mobile or password incorrect"));
        Long userId = account.getUserId();
        authPersistenceService.touchLoginDevice(userId, request.deviceId(), "app");
        return ApiResponse.ok(tokenData(userId, request.deviceId()));
    }

    @PostMapping("/login/sms")
    public ApiResponse<LoginTokenData> loginBySms(@Valid @RequestBody SmsLoginRequest request) {
        UserAccountEntity account = authPersistenceService.findAccountByMobile(request.mobile())
                .orElseGet(() -> authPersistenceService.registerAccount(request.mobile(), "sms-login-default", "app", request.deviceId()));
        Long userId = account.getUserId();
        authPersistenceService.touchLoginDevice(userId, request.deviceId(), "app");
        return ApiResponse.ok(tokenData(userId, request.deviceId()));
    }

    @PostMapping("/register")
    public ApiResponse<LoginTokenData> register(@Valid @RequestBody RegisterRequest request) {
        String deviceId = request.deviceId() == null || request.deviceId().isBlank() ? "register-" + request.mobile() : request.deviceId();
        UserAccountEntity account = authPersistenceService.registerAccount(request.mobile(), request.password(), "app", deviceId);
        return ApiResponse.ok(tokenData(account.getUserId(), deviceId));
    }

    @PostMapping("/sms/send")
    public ApiResponse<Void> sendSms(@Valid @RequestBody SmsSendRequest request) {
        return ApiResponse.ok();
    }

    @PostMapping("/token/refresh")
    public ApiResponse<LoginTokenData> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        TokenPayload payload = tokenService.requireRefreshToken(request.refreshToken());
        return ApiResponse.ok(tokenData(payload.userId(), payload.deviceId()));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody LogoutRequest request) {
        authPersistenceService.offlineDevice(request.deviceId());
        return ApiResponse.ok();
    }

    @GetMapping("/device/list")
    public ApiResponse<List<DeviceSession>> listDevices() {
        List<DeviceSession> deviceList = authPersistenceService.listUserDevices(UserContext.requireUserId()).stream()
                .map(this::toDeviceSession)
                .toList();
        if (!deviceList.isEmpty()) {
            return ApiResponse.ok(deviceList);
        }
        return ApiResponse.ok(List.of(
                new DeviceSession("device-app-001", "android", "Xiaomi 14", "Android 15", "Shenzhen", "120.22.11.10", Instant.now().toString(), true),
                new DeviceSession("device-web-001", "web", "Chrome", "126.0", "Guangzhou", "183.2.10.21", Instant.now().minusSeconds(3600).toString(), false)
        ));
    }

    @PostMapping("/device/offline")
    public ApiResponse<Void> offlineDevice(@Valid @RequestBody DeviceOfflineRequest request) {
        authPersistenceService.offlineDevice(request.deviceId());
        return ApiResponse.ok();
    }

    private LoginTokenData tokenData(Long userId, String deviceId) {
        return new LoginTokenData(
                userId,
                tokenService.createAccessToken(userId, deviceId),
                tokenService.createRefreshToken(userId, deviceId),
                7200,
                false
        );
    }

    private DeviceSession toDeviceSession(UserDeviceEntity entity) {
        return new DeviceSession(
                entity.getDeviceId(),
                entity.getDeviceType(),
                entity.getDeviceModel(),
                entity.getSystemVersion(),
                entity.getLoginCity(),
                entity.getLoginIp(),
                entity.getLastLoginAt() == null ? null : entity.getLastLoginAt().toString(),
                entity.getOnlineStatus() != null && entity.getOnlineStatus() == 1
        );
    }

    public record PasswordLoginRequest(@NotBlank String mobile, @NotBlank String password, @NotBlank String deviceId) {
    }

    public record SmsLoginRequest(@NotBlank String mobile, @NotBlank String smsCode, @NotBlank String deviceId) {
    }

    public record RegisterRequest(@NotBlank String mobile, @NotBlank String smsCode, @NotBlank String password, String deviceId) {
    }

    public record SmsSendRequest(@NotBlank String mobile, @NotBlank String scene) {
    }

    public record RefreshTokenRequest(@NotBlank String refreshToken) {
    }

    public record LogoutRequest(@NotBlank String deviceId) {
    }

    public record DeviceOfflineRequest(@NotBlank String deviceId) {
    }

    public record LoginTokenData(Long userId, String accessToken, String refreshToken, Integer expireIn, Boolean needDeviceVerify) {
    }

    public record DeviceSession(String deviceId, String deviceType, String deviceModel, String systemVersion,
                                String loginCity, String loginIp, String lastLoginAt, Boolean online) {
    }
}
