package com.weiyou.feature.controller;

import com.weiyou.common.core.api.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/feature")
public class FeatureController {

    @GetMapping("/discovery")
    public ApiResponse<List<FeatureItem>> discovery(@RequestParam(required = false) String cityCode) {
        return ApiResponse.ok(List.of(
                new FeatureItem("moment", "朋友圈", "https://weiyou.local/icons/moment.png", "/pages/moments/index", true),
                new FeatureItem("scan", "扫一扫", "https://weiyou.local/icons/scan.png", "/pages/scan/index", true),
                new FeatureItem("search", "搜一搜", "https://weiyou.local/icons/search.png", "/pages/search/index", true)
        ));
    }

    @GetMapping("/workbench")
    public ApiResponse<List<FeatureItem>> workbench(@RequestParam(required = false) String scene) {
        return ApiResponse.ok(List.of(
                new FeatureItem("miniapp_recent", "最近小程序", "https://weiyou.local/icons/miniapp.png", "/pages/miniapp/recent", true),
                new FeatureItem("miniapp_favorites", "收藏小程序", "https://weiyou.local/icons/miniapp-favorite.png", "/pages/miniapp/favorites", true),
                new FeatureItem("official", "公众号", "https://weiyou.local/icons/official.png", "/pages/official/list", true),
                new FeatureItem("wallet", "钱包", "https://weiyou.local/icons/wallet.png", "/pages/wallet/index", true)
        ));
    }

    public record FeatureItem(String featureCode, String featureName, String iconUrl, String routePath, Boolean enabled) {
    }
}
