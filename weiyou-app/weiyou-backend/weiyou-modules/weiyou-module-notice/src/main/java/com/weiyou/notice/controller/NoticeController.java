package com.weiyou.notice.controller;

import com.weiyou.common.core.api.ApiResponse;
import com.weiyou.common.core.api.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notice")
public class NoticeController {

    private final Map<Long, Integer> readState = new ConcurrentHashMap<>();

    @GetMapping("/list")
    public ApiResponse<PageResponse<NoticeItem>> list(@RequestParam(required = false) String type,
                                                      @RequestParam(required = false) String cursor) {
        List<NoticeItem> list = List.of(
                new NoticeItem(40001L, "system", "系统通知", "欢迎使用微友后端骨架", readStatus(40001L), Instant.now().toString()),
                new NoticeItem(40002L, "moment", "朋友圈提醒", "阿泽评论了你的动态", readStatus(40002L), Instant.now().minusSeconds(90).toString())
        );
        if (type == null || type.isBlank()) {
            return ApiResponse.ok(PageResponse.of(list, 1, 20, list.size(), false, null));
        }
        List<NoticeItem> filtered = list.stream().filter(item -> item.noticeType().equalsIgnoreCase(type)).toList();
        return ApiResponse.ok(PageResponse.of(filtered, 1, 20, filtered.size(), false, null));
    }

    @PostMapping("/read")
    public ApiResponse<NoticeReadResult> read(@Valid @RequestBody NoticeReadRequest request) {
        readState.put(request.noticeId(), 1);
        return ApiResponse.ok(new NoticeReadResult(request.noticeId(), 1));
    }

    public record NoticeItem(Long noticeId, String noticeType, String title, String content,
                             Integer readStatus, String createdAt) {
    }

    public record NoticeReadRequest(@NotNull Long noticeId) {
    }

    public record NoticeReadResult(Long noticeId, Integer readStatus) {
    }

    private Integer readStatus(Long noticeId) {
        return readState.getOrDefault(noticeId, 0);
    }
}
