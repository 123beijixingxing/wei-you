package com.weiyou.feature.controller;

import com.weiyou.common.core.api.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import java.util.Objects;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scan")
public class ScanController {

    @PostMapping("/resolve")
    public ApiResponse<ScanResult> resolve(@Valid @RequestBody ScanResolveRequest request) {
        String code = Objects.toString(request.scanCode(), "").trim();
        if (code.startsWith("user:") || code.contains("/user/")) {
            Long userId = parseLong(code.replace("user:", "").replace("weiyou://qrcode/user/", ""), 10002L);
            return ApiResponse.ok(new ScanResult("user_qrcode", "scan-user-" + userId, "微友名片", "open_profile",
                    Map.of("userId", userId, "routePath", "/pages/contacts/profile?id=" + userId)));
        }
        if (code.startsWith("group:") || code.contains("/group/")) {
            Long groupId = parseLong(code.replace("group:", "").replace("weiyou://qrcode/group/", ""), 90002L);
            return ApiResponse.ok(new ScanResult("group_qrcode", "scan-group-" + groupId, "群聊邀请", "open_group",
                    Map.of("groupId", groupId, "routePath", "/pages/group/detail?groupId=" + groupId)));
        }
        if (code.startsWith("official:")) {
            Long officialId = parseLong(code.replace("official:", ""), 20001L);
            return ApiResponse.ok(new ScanResult("official_account", "scan-official-" + officialId, "公众号", "open_official",
                    Map.of("officialId", officialId, "routePath", "/pages/official/detail?officialId=" + officialId)));
        }
        if (code.startsWith("miniapp:")) {
            String appId = code.replace("miniapp:", "").trim();
            return ApiResponse.ok(new ScanResult("miniapp_code", "scan-miniapp-" + appId, "小程序", "open_miniapp",
                    Map.of("appId", appId, "routePath", "/pages/miniapp/open?appId=" + appId)));
        }
        if (code.startsWith("pay:")) {
            Long targetUserId = parseLong(code.replace("pay:", ""), 10002L);
            return ApiResponse.ok(new ScanResult("payment_code", "scan-pay-" + targetUserId, "收付款", "open_transfer",
                    Map.of("targetUserId", targetUserId, "routePath", "/pages/wallet/transfer?targetUserId=" + targetUserId)));
        }
        if (code.startsWith("http://") || code.startsWith("https://")) {
            return ApiResponse.ok(new ScanResult("web_link", "scan-link", "外部链接", "show_link",
                    Map.of("url", code, "routePath", "")));
        }
        return ApiResponse.ok(new ScanResult("unknown_text", "scan-raw", "原始内容", "show_raw",
                Map.of("content", code, "routePath", "")));
    }

    private Long parseLong(String value, Long fallback) {
        try {
            return Long.parseLong(value.trim());
        } catch (Exception exception) {
            return fallback;
        }
    }

    public record ScanResolveRequest(@NotBlank String scanCode, String scene) {
    }

    public record ScanResult(String scanType, String scanToken, String title, String actionType, Map<String, Object> payload) {
    }
}
