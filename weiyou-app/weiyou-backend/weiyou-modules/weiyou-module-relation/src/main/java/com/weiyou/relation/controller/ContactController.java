package com.weiyou.relation.controller;

import com.weiyou.common.core.api.ApiResponse;
import com.weiyou.common.core.api.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/contact")
public class ContactController {

    private static final AtomicLong REQUEST_ID_SEQUENCE = new AtomicLong(50010L);

    private final CopyOnWriteArrayList<SearchUserItem> searchDirectory = new CopyOnWriteArrayList<>(List.of(
            new SearchUserItem(10008L, "小宇", "wy_xiaoyu", "https://weiyou.local/avatar/10008.png", "深圳", "来自项目群的新同学"),
            new SearchUserItem(10009L, "Ada", "wy_ada", "https://weiyou.local/avatar/10009.png", "上海", "喜欢研究产品增长"),
            new SearchUserItem(10010L, "Bob", "wy_bob", "https://weiyou.local/avatar/10010.png", "杭州", "后端工程师")
    ));

    private final CopyOnWriteArrayList<ContactItem> contacts = new CopyOnWriteArrayList<>(List.of(
            new ContactItem(10002L, "阿泽", "同事阿泽", "https://weiyou.local/avatar/10002.png", "A", true),
            new ContactItem(10003L, "小林", "产品小林", "https://weiyou.local/avatar/10003.png", "X", false)
    ));

    private final CopyOnWriteArrayList<FriendRequestItem> friendRequests = new CopyOnWriteArrayList<>(List.of(
            new FriendRequestItem(50001L, 10008L, "小宇", "https://weiyou.local/avatar/10008.png", "来自项目群", 0, Instant.now().toString())
    ));

    @GetMapping("/list")
    public ApiResponse<PageResponse<ContactItem>> listContacts(@RequestParam(required = false) String keyword,
                                                               @RequestParam(required = false) String letter) {
        List<ContactItem> list = new ArrayList<>(contacts);
        if (keyword != null && !keyword.isBlank()) {
            String text = keyword.trim().toLowerCase();
            list = list.stream().filter(item -> item.nickname().toLowerCase().contains(text)
                    || item.remark().toLowerCase().contains(text)).toList();
        }
        if (letter != null && !letter.isBlank()) {
            String normalizedLetter = letter.trim().toUpperCase();
            list = list.stream().filter(item -> item.letter().equalsIgnoreCase(normalizedLetter)).toList();
        }
        return ApiResponse.ok(PageResponse.of(list, 1, 20, list.size(), false, null));
    }

    @PostMapping("/friend/apply")
    public ApiResponse<FriendRequestItem> applyFriend(@Valid @RequestBody FriendApplyRequest request) {
        SearchUserItem target = searchDirectory.stream()
                .filter(item -> item.userId().equals(request.targetUserId()))
                .findFirst()
                .orElse(new SearchUserItem(request.targetUserId(), "新朋友", "wy_new_friend", "https://weiyou.local/avatar/default.png", "未知", "新的好友申请"));
        FriendRequestItem item = new FriendRequestItem(
                REQUEST_ID_SEQUENCE.incrementAndGet(),
                request.targetUserId(),
                target.nickname(),
                target.avatar(),
                request.remark() == null || request.remark().isBlank() ? "你好，我想添加你为朋友" : request.remark(),
                0,
                Instant.now().toString()
        );
        friendRequests.add(0, item);
        return ApiResponse.ok(item);
    }

    @GetMapping("/search")
    public ApiResponse<List<SearchUserItem>> search(@RequestParam String keyword) {
        String text = keyword.trim().toLowerCase();
        List<SearchUserItem> list = searchDirectory.stream()
                .filter(item -> item.nickname().toLowerCase().contains(text)
                        || item.weiyouNo().toLowerCase().contains(text)
                        || item.city().toLowerCase().contains(text))
                .toList();
        return ApiResponse.ok(list);
    }

    @GetMapping("/friend/request/list")
    public ApiResponse<PageResponse<FriendRequestItem>> listFriendRequests(@RequestParam(required = false) Integer status) {
        List<FriendRequestItem> list = new ArrayList<>(friendRequests);
        if (status != null) {
            list = list.stream().filter(item -> item.status().equals(status)).toList();
        }
        return ApiResponse.ok(PageResponse.of(list, 1, 20, list.size(), false, null));
    }

    @PostMapping("/friend/request/handle")
    public ApiResponse<FriendRequestItem> handleFriendRequest(@Valid @RequestBody HandleFriendRequest request) {
        FriendRequestItem target = friendRequests.stream()
                .filter(item -> item.requestId().equals(request.requestId()))
                .findFirst()
                .orElse(null);
        if (target == null) {
            return ApiResponse.ok(null);
        }
        Integer nextStatus = "accept".equalsIgnoreCase(request.action()) ? 1 : 2;
        FriendRequestItem updated = new FriendRequestItem(
                target.requestId(),
                target.fromUserId(),
                target.nickname(),
                target.avatar(),
                target.applyMessage(),
                nextStatus,
                target.createdAt()
        );
        friendRequests.removeIf(item -> item.requestId().equals(request.requestId()));
        friendRequests.add(0, updated);
        if (nextStatus == 1) {
            ensureContact(updated.fromUserId(), updated.nickname(), updated.avatar());
        }
        return ApiResponse.ok(updated);
    }

    private void ensureContact(Long userId, String nickname, String avatar) {
        boolean exists = contacts.stream().anyMatch(item -> item.userId().equals(userId));
        if (exists) {
            return;
        }
        String remark = nickname == null || nickname.isBlank() ? "新朋友" : nickname;
        String letter = remark.substring(0, 1).toUpperCase();
        contacts.add(0, new ContactItem(userId, remark, remark, avatar == null || avatar.isBlank() ? "https://weiyou.local/avatar/default.png" : avatar, letter, false));
    }

    public record FriendApplyRequest(@NotNull Long targetUserId, String remark, String source) {
    }

    public record HandleFriendRequest(@NotNull Long requestId, @NotBlank String action) {
    }

    public record ContactItem(Long userId, String nickname, String remark, String avatar, String letter, Boolean star) {
    }

    public record FriendRequestItem(Long requestId, Long fromUserId, String nickname, String avatar,
                                     String applyMessage, Integer status, String createdAt) {
    }

    public record SearchUserItem(Long userId, String nickname, String weiyouNo, String avatar, String city, String summary) {
    }
}
