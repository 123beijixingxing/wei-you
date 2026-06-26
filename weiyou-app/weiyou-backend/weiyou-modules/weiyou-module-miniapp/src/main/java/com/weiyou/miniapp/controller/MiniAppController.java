package com.weiyou.miniapp.controller;

import com.weiyou.common.core.api.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/miniapp")
public class MiniAppController {

    private final CopyOnWriteArrayList<String> favoriteAppIds = new CopyOnWriteArrayList<>(List.of("miniapp-demo-001"));
    private final CopyOnWriteArrayList<MiniAppRecentItem> recentItems = new CopyOnWriteArrayList<>(List.of(
            new MiniAppRecentItem("miniapp-demo-001", "微友商城", "https://weiyou.local/miniapp/mall.png", "/pages/index/index", Instant.now().minusSeconds(1800).toString(), true),
            new MiniAppRecentItem("miniapp-demo-002", "微友日程", "https://weiyou.local/miniapp/schedule.png", "/pages/home/index", Instant.now().minusSeconds(5400).toString(), false)
    ));

    @GetMapping("/recent/list")
    public ApiResponse<List<MiniAppRecentItem>> recentList() {
        return ApiResponse.ok(List.copyOf(recentItems));
    }

    @GetMapping("/favorite/list")
    public ApiResponse<List<MiniAppRecentItem>> favoriteList() {
        return ApiResponse.ok(recentItems.stream().filter(MiniAppRecentItem::favorite).toList());
    }

    @PostMapping("/open")
    public ApiResponse<MiniAppSession> open(@Valid @RequestBody MiniAppOpenRequest request) {
        touchRecentItem(request.appId(), request.path());
        return ApiResponse.ok(new MiniAppSession(request.appId(), "微友商城", "miniapp-session-demo-token",
                request.path() == null ? "/pages/index/index" : request.path(), "2026-12-31T23:59:59Z", favoriteAppIds.contains(request.appId())));
    }

    @PostMapping("/recent/remove")
    public ApiResponse<List<MiniAppRecentItem>> removeRecent(@Valid @RequestBody MiniAppRecentRemoveRequest request) {
        recentItems.removeIf(item -> item.appId().equals(request.appId()));
        return ApiResponse.ok(List.copyOf(recentItems));
    }

    @PostMapping("/favorite/toggle")
    public ApiResponse<MiniAppFavoriteResult> toggleFavorite(@Valid @RequestBody MiniAppFavoriteRequest request) {
        boolean favorite;
        if ("unfavorite".equalsIgnoreCase(request.action())) {
            favoriteAppIds.removeIf(appId -> appId.equals(request.appId()));
            favorite = false;
        } else {
            if (!favoriteAppIds.contains(request.appId())) {
                favoriteAppIds.add(request.appId());
            }
            favorite = true;
        }
        refreshFavoriteState();
        return ApiResponse.ok(new MiniAppFavoriteResult(request.appId(), request.action(), favorite));
    }

    public record MiniAppOpenRequest(@NotBlank String appId, String path, String scene) {
    }

    public record MiniAppSession(String appId, String appName, String sessionToken, String path, String expireAt, Boolean favorite) {
    }

    public record MiniAppRecentItem(String appId, String appName, String iconUrl, String path, String lastUsedAt, Boolean favorite) {
    }

    public record MiniAppRecentRemoveRequest(@NotBlank String appId) {
    }

    public record MiniAppFavoriteRequest(@NotBlank String appId, @NotBlank String action) {
    }

    public record MiniAppFavoriteResult(String appId, String action, Boolean favorite) {
    }

    private void touchRecentItem(String appId, String path) {
        List<MiniAppRecentItem> snapshot = new ArrayList<>(recentItems);
        snapshot.removeIf(item -> item.appId().equals(appId));
        String appName = "miniapp-demo-002".equals(appId) ? "微友日程" : "微友商城";
        String iconUrl = "miniapp-demo-002".equals(appId)
                ? "https://weiyou.local/miniapp/schedule.png"
                : "https://weiyou.local/miniapp/mall.png";
        snapshot.add(0, new MiniAppRecentItem(appId, appName, iconUrl,
                path == null ? "/pages/index/index" : path,
                Instant.now().toString(), favoriteAppIds.contains(appId)));
        recentItems.clear();
        recentItems.addAll(snapshot.stream().limit(10).toList());
    }

    private void refreshFavoriteState() {
        List<MiniAppRecentItem> snapshot = new ArrayList<>(recentItems);
        recentItems.clear();
        recentItems.addAll(snapshot.stream().map(item -> new MiniAppRecentItem(
                item.appId(),
                item.appName(),
                item.iconUrl(),
                item.path(),
                item.lastUsedAt(),
                favoriteAppIds.contains(item.appId())
        )).toList());
    }
}
