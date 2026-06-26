package com.weiyou.feature.controller;

import com.weiyou.common.core.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app")
public class AppController {

    @GetMapping("/bootstrap")
    public ApiResponse<BootstrapData> bootstrap(@RequestParam String clientType, @RequestParam String version) {
        return ApiResponse.ok(new BootstrapData("0.1.0", false, "2026.05", "welcome to weiyou"));
    }

    public record BootstrapData(String latestVersion, Boolean forceUpgrade, String agreementVersion, String noticeText) {
    }
}
