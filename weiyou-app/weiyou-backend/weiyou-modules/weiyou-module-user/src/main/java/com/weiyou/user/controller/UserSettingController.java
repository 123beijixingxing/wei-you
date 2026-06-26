package com.weiyou.user.controller;

import com.weiyou.common.core.api.ApiResponse;
import com.weiyou.common.security.context.UserContext;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/setting")
public class UserSettingController {

    private final Map<Long, UserSettingData> settingStore = new ConcurrentHashMap<>();

    @GetMapping("/detail")
    public ApiResponse<UserSettingData> getDetail() {
        Long userId = UserContext.requireUserId();
        return ApiResponse.ok(resolve(userId));
    }

    @PostMapping("/update")
    public ApiResponse<UserSettingData> update(@Valid @RequestBody UpdateUserSettingRequest request) {
        Long userId = UserContext.requireUserId();
        UserSettingData current = resolve(userId);
        UserSettingData next = new UserSettingData(
                userId,
                request.messageNotification() == null ? current.messageNotification() : request.messageNotification(),
                request.momentNotification() == null ? current.momentNotification() : request.momentNotification(),
                request.officialNotification() == null ? current.officialNotification() : request.officialNotification(),
                request.soundEnabled() == null ? current.soundEnabled() : request.soundEnabled(),
                request.vibrationEnabled() == null ? current.vibrationEnabled() : request.vibrationEnabled(),
                request.addByPhone() == null ? current.addByPhone() : request.addByPhone(),
                request.addByWeiyouNo() == null ? current.addByWeiyouNo() : request.addByWeiyouNo(),
                request.groupInviteConfirm() == null ? current.groupInviteConfirm() : request.groupInviteConfirm(),
                request.hideMyMoments() == null ? current.hideMyMoments() : request.hideMyMoments(),
                request.hideLocation() == null ? current.hideLocation() : request.hideLocation()
        );
        settingStore.put(userId, next);
        return ApiResponse.ok(next);
    }

    private UserSettingData resolve(Long userId) {
        return settingStore.computeIfAbsent(userId, id -> new UserSettingData(
                id,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                false,
                false,
                false
        ));
    }

    public record UpdateUserSettingRequest(Boolean messageNotification,
                                           Boolean momentNotification,
                                           Boolean officialNotification,
                                           Boolean soundEnabled,
                                           Boolean vibrationEnabled,
                                           Boolean addByPhone,
                                           Boolean addByWeiyouNo,
                                           Boolean groupInviteConfirm,
                                           Boolean hideMyMoments,
                                           Boolean hideLocation) {
    }

    public record UserSettingData(Long userId,
                                  Boolean messageNotification,
                                  Boolean momentNotification,
                                  Boolean officialNotification,
                                  Boolean soundEnabled,
                                  Boolean vibrationEnabled,
                                  Boolean addByPhone,
                                  Boolean addByWeiyouNo,
                                  Boolean groupInviteConfirm,
                                  Boolean hideMyMoments,
                                  Boolean hideLocation) {
    }
}
