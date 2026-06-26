package com.weiyou.user.controller;

import com.weiyou.user.app.service.UserPersistenceService;
import com.weiyou.user.domain.entity.UserProfileEntity;
import com.weiyou.common.core.api.ApiResponse;
import com.weiyou.common.security.context.UserContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserProfileController {

    private final UserPersistenceService userPersistenceService;

    public UserProfileController(UserPersistenceService userPersistenceService) {
        this.userPersistenceService = userPersistenceService;
    }

    @GetMapping("/profile/me")
    public ApiResponse<UserProfileData> getMyProfile() {
        return ApiResponse.ok(profile(UserContext.requireUserId()));
    }

    @GetMapping("/profile/detail")
    public ApiResponse<UserProfileData> getProfileDetail(@RequestParam(required = false) Long userId) {
        return ApiResponse.ok(profile(userId == null ? UserContext.requireUserId() : userId));
    }

    @PostMapping("/profile/update")
    public ApiResponse<Void> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        userPersistenceService.saveOrUpdateProfile(UserContext.requireUserId(), request.nickname(), request.avatar(), request.gender(), request.city(), request.signature());
        return ApiResponse.ok();
    }

    @GetMapping("/qrcode/get")
    public ApiResponse<QrcodeData> getQrcode(@RequestParam(defaultValue = "false") boolean dynamic) {
        Long userId = UserContext.requireUserId();
        return ApiResponse.ok(new QrcodeData("ticket-" + userId, "https://weiyou.local/qrcode/" + userId + ".png", dynamic, "2026-12-31T23:59:59Z"));
    }

    @PostMapping("/status/update")
    public ApiResponse<Void> updateStatus(@Valid @RequestBody UpdateStatusRequest request) {
        userPersistenceService.updateStatus(UserContext.requireUserId(), request.statusCode(), request.statusText(), request.expireAt());
        return ApiResponse.ok();
    }

    private UserProfileData profile(Long userId) {
        UserProfileEntity entity = userPersistenceService.findProfileByUserId(userId).orElse(null);
        if (entity != null) {
            String city = entity.getCityName() == null ? entity.getProvinceName() : entity.getCityName();
            return new UserProfileData(
                    userId,
                    "weiyou_" + userId,
                    entity.getNickname(),
                    entity.getAvatarUrl(),
                    entity.getGender(),
                    city,
                    entity.getSignature(),
                    entity.getStatusText(),
                    "https://weiyou.local/qrcode/" + userId + ".png"
            );
        }
        return new UserProfileData(userId, "weiyou_10001", "微友产品体验官", "https://weiyou.local/avatar/10001.png",
                1, "深圳", "让连接更近一点", "开会中", "https://weiyou.local/qrcode/10001.png");
    }

    public record UpdateProfileRequest(String nickname, String avatar, Integer gender, String city, String signature) {
    }

    public record UpdateStatusRequest(@NotBlank String statusCode, String statusText, String expireAt) {
    }

    public record UserProfileData(Long userId, String weiyouNo, String nickname, String avatar,
                                  Integer gender, String city, String signature, String statusText, String qrcodeUrl) {
    }

    public record QrcodeData(String ticket, String qrcodeUrl, Boolean dynamic, String expireAt) {
    }
}
