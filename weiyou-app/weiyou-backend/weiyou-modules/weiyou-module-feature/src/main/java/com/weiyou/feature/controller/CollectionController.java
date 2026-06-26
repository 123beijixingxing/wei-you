package com.weiyou.feature.controller;

import com.weiyou.common.core.api.ApiResponse;
import com.weiyou.common.core.api.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/collection")
public class CollectionController {

    private final CopyOnWriteArrayList<CollectionItem> collectionItems = new CopyOnWriteArrayList<>(List.of(
            new CollectionItem(700001L, "link", "微友版本更新周报", "https://weiyou.local/official/article-cover-1.png", "公众号文章收藏", Instant.now().minusSeconds(3600).toString()),
            new CollectionItem(700002L, "image", "朋友圈精选图片", "https://weiyou.local/moment/30001-1.png", "朋友圈图片收藏", Instant.now().minusSeconds(7200).toString()),
            new CollectionItem(700003L, "note", "跨端同步笔记", null, "支持同步文本、链接、文件", Instant.now().minusSeconds(10800).toString())
    ));

    @GetMapping("/list")
    public ApiResponse<PageResponse<CollectionItem>> list(@RequestParam(required = false) String type,
                                                          @RequestParam(required = false) String keyword,
                                                          @RequestParam(defaultValue = "1") int pageNo) {
        List<CollectionItem> list = collectionItems.stream()
                .filter(item -> type == null || type.isBlank() || item.type().equalsIgnoreCase(type))
                .filter(item -> keyword == null || keyword.isBlank()
                        || item.title().toLowerCase().contains(keyword.trim().toLowerCase())
                        || item.summary().toLowerCase().contains(keyword.trim().toLowerCase()))
                .toList();
        return ApiResponse.ok(PageResponse.of(list, pageNo, 20, list.size(), false, null));
    }

    @PostMapping("/delete")
    public ApiResponse<List<CollectionItem>> delete(@Valid @RequestBody CollectionDeleteRequest request) {
        collectionItems.removeIf(item -> item.collectionId().equals(request.collectionId()));
        return ApiResponse.ok(List.copyOf(collectionItems));
    }

    @PostMapping("/create")
    public ApiResponse<CollectionItem> create(@Valid @RequestBody CollectionCreateRequest request) {
        CollectionItem item = new CollectionItem(
                System.currentTimeMillis(),
                request.type(),
                request.title(),
                request.cover(),
                request.summary(),
                Instant.now().toString()
        );
        collectionItems.add(0, item);
        return ApiResponse.ok(item);
    }

    public record CollectionItem(Long collectionId, String type, String title, String cover, String summary, String createdAt) {
    }

    public record CollectionDeleteRequest(@NotNull Long collectionId) {
    }

    public record CollectionCreateRequest(@NotBlank String type,
                                         @NotBlank String title,
                                         String cover,
                                         String summary) {
    }
}
