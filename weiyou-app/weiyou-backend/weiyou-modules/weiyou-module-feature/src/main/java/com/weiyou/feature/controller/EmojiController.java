package com.weiyou.feature.controller;

import com.weiyou.common.core.api.ApiResponse;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/emoji")
public class EmojiController {

    private final CopyOnWriteArrayList<EmojiPackageItem> packages = new CopyOnWriteArrayList<>(List.of(
            new EmojiPackageItem("emoji-pack-001", "微友默认表情", "轻松沟通的一组常用表情", true, true),
            new EmojiPackageItem("emoji-pack-002", "办公摸鱼包", "适合群聊和同事斗图", false, false),
            new EmojiPackageItem("emoji-pack-003", "周末出游包", "适合出游、聚会、拍照分享", false, false)
    ));

    @GetMapping("/store/list")
    public ApiResponse<List<EmojiPackageItem>> list(@RequestParam(required = false) String categoryId,
                                                    @RequestParam(required = false) Integer pageNo) {
        return ApiResponse.ok(List.copyOf(packages));
    }

    @PostMapping("/package/download")
    public ApiResponse<EmojiPackageItem> download(@RequestBody EmojiPackageActionRequest request) {
        EmojiPackageItem updated = packages.stream()
                .filter(item -> item.packageId().equals(request.packageId()))
                .findFirst()
                .map(item -> new EmojiPackageItem(item.packageId(), item.title(), item.summary(), true, item.active()))
                .orElse(null);
        if (updated != null) {
            packages.removeIf(item -> item.packageId().equals(request.packageId()));
            packages.add(0, updated);
        }
        return ApiResponse.ok(updated);
    }

    @PostMapping("/package/remove")
    public ApiResponse<EmojiPackageItem> remove(@RequestBody EmojiPackageActionRequest request) {
        EmojiPackageItem updated = packages.stream()
                .filter(item -> item.packageId().equals(request.packageId()))
                .findFirst()
                .map(item -> new EmojiPackageItem(item.packageId(), item.title(), item.summary(), false, false))
                .orElse(null);
        if (updated != null) {
            packages.removeIf(item -> item.packageId().equals(request.packageId()));
            packages.add(updated);
        }
        return ApiResponse.ok(updated);
    }

    @PostMapping("/package/activate")
    public ApiResponse<List<EmojiPackageItem>> activate(@RequestBody EmojiPackageActionRequest request) {
        List<EmojiPackageItem> updated = packages.stream()
                .map(item -> new EmojiPackageItem(
                        item.packageId(),
                        item.title(),
                        item.summary(),
                        item.downloaded(),
                        item.packageId().equals(request.packageId()) && item.downloaded()
                ))
                .toList();
        packages.clear();
        packages.addAll(updated);
        return ApiResponse.ok(updated);
    }

    public record EmojiPackageItem(String packageId, String title, String summary, Boolean downloaded, Boolean active) {
    }

    public record EmojiPackageActionRequest(String packageId) {
    }
}
