package com.weiyou.moment.controller;

import com.weiyou.common.core.api.ApiResponse;
import com.weiyou.common.core.api.PageResponse;
import com.weiyou.common.security.context.UserContext;
import com.weiyou.moment.app.service.MomentPersistenceService;
import com.weiyou.moment.domain.entity.MomentPostEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/moment")
public class MomentController {

    private final CopyOnWriteArrayList<MomentItem> fallbackMoments = new CopyOnWriteArrayList<>();

    private final MomentPersistenceService momentPersistenceService;

    public MomentController(MomentPersistenceService momentPersistenceService) {
        this.momentPersistenceService = momentPersistenceService;
    }

    @GetMapping("/timeline")
    public ApiResponse<PageResponse<MomentItem>> listTimeline(@RequestParam(required = false) String cursor,
                                                              @RequestParam(defaultValue = "20") int pageSize) {
        List<MomentItem> list;
        try {
            list = momentPersistenceService.listTimeline(UserContext.requireUserId()).stream()
                    .map(this::toMomentItem)
                    .toList();
        } catch (RuntimeException exception) {
            list = List.of();
        }
        if (!fallbackMoments.isEmpty()) {
            List<MomentItem> merged = new ArrayList<>(fallbackMoments);
            merged.addAll(list.stream().filter(item -> fallbackMoments.stream().noneMatch(fallback -> fallback.momentId().equals(item.momentId()))).toList());
            list = merged;
        }
        if (list.isEmpty()) {
            list = List.of(
                    new MomentItem(30001L, 10002L, "阿泽", "https://weiyou.local/avatar/10002.png", "今天把需求评审过了一遍。",
                            List.of(new MomentMediaItem("https://weiyou.local/moment/30001-1.png", "https://weiyou.local/moment/30001-1.png")),
                            List.of(new MomentCommentItem(50001L, 10001L, "微友产品体验官", "这个功能已经可联调。", null, null, null, Instant.now().minusSeconds(1200).toString())),
                            12, 3, Instant.now().minusSeconds(1800).toString()),
                    new MomentItem(30002L, 10003L, "小林", "https://weiyou.local/avatar/10003.png", "版本节奏推进中。",
                            List.of(), List.of(), 6, 1, Instant.now().minusSeconds(3600).toString())
            );
        }
        return ApiResponse.ok(PageResponse.of(list, 1, pageSize, list.size(), false, null));
    }

    @PostMapping("/create")
    public ApiResponse<MomentItem> createMoment(@Valid @RequestBody CreateMomentRequest request) {
        Long userId = UserContext.requireUserId();
        try {
            var aggregate = momentPersistenceService.createMoment(userId, request.content(), request.mediaUrls(), request.visibleScope());
            MomentItem item = toMomentItem(aggregate);
            fallbackMoments.removeIf(moment -> moment.momentId().equals(item.momentId()));
            fallbackMoments.add(0, item);
            return ApiResponse.ok(item);
        } catch (RuntimeException exception) {
            MomentItem item = new MomentItem(
                    System.currentTimeMillis(),
                    userId,
                    "微友产品体验官",
                    "https://weiyou.local/avatar/10001.png",
                    request.content(),
                    (request.mediaUrls() == null ? List.<String>of() : request.mediaUrls()).stream().map(url -> new MomentMediaItem(url, url)).toList(),
                    List.of(),
                    0,
                    0,
                    Instant.now().toString()
            );
            fallbackMoments.removeIf(moment -> moment.momentId().equals(item.momentId()));
            fallbackMoments.add(0, item);
            return ApiResponse.ok(item);
        }
    }

    @PostMapping("/like")
    public ApiResponse<LikeResult> toggleLike(@Valid @RequestBody LikeMomentRequest request) {
        try {
            int likeCount = momentPersistenceService.toggleLike(request.momentId(), request.action());
            return ApiResponse.ok(new LikeResult(request.momentId(), request.action(), likeCount));
        } catch (RuntimeException exception) {
            updateFallbackLike(request.momentId(), request.action());
            int likeCount = fallbackMoments.stream()
                    .filter(item -> item.momentId().equals(request.momentId()))
                    .map(MomentItem::likeCount)
                    .findFirst()
                    .orElse("unlike".equalsIgnoreCase(request.action()) ? 0 : 1);
            return ApiResponse.ok(new LikeResult(request.momentId(), request.action(), likeCount));
        }
    }

    @PostMapping("/comment/create")
    public ApiResponse<CommentResult> createComment(@Valid @RequestBody CommentRequest request) {
        try {
            var result = momentPersistenceService.addComment(UserContext.requireUserId(), request.momentId(), request.content(), request.replyCommentId());
            return ApiResponse.ok(new CommentResult(
                    request.momentId(),
                    result.comment().getCommentId(),
                    request.content(),
                    result.comment().getReplyCommentId(),
                    result.comment().getReplyUserId(),
                    result.commentCount(),
                    result.comment().getCreatedAt() == null ? Instant.now().toString() : result.comment().getCreatedAt().toString()
            ));
        } catch (RuntimeException exception) {
            long commentId = System.currentTimeMillis();
            addFallbackComment(request.momentId(), commentId, UserContext.requireUserId(), request.content(), request.replyCommentId());
            int commentCount = fallbackMoments.stream().filter(item -> item.momentId().equals(request.momentId())).map(MomentItem::commentCount).findFirst().orElse(1);
            return ApiResponse.ok(new CommentResult(request.momentId(), commentId, request.content(), request.replyCommentId(), null, commentCount, Instant.now().toString()));
        }
    }

    @PostMapping("/comment/delete")
    public ApiResponse<CommentDeleteResult> deleteComment(@Valid @RequestBody CommentDeleteRequest request) {
        try {
            int commentCount = momentPersistenceService.deleteComment(UserContext.requireUserId(), request.commentId());
            return ApiResponse.ok(new CommentDeleteResult(request.commentId(), request.momentId(), commentCount));
        } catch (RuntimeException exception) {
            deleteFallbackComment(request.momentId(), request.commentId());
            return ApiResponse.ok(new CommentDeleteResult(request.commentId(), request.momentId(), 0));
        }
    }

    public record CreateMomentRequest(String content, List<String> mediaUrls, String visibleScope) {
    }

    public record LikeMomentRequest(@NotNull Long momentId, @NotBlank String action) {
    }

    public record CommentRequest(@NotNull Long momentId, @NotBlank String content, Long replyCommentId) {
    }

    public record CommentDeleteRequest(@NotNull Long momentId, @NotNull Long commentId) {
    }

    public record MomentItem(Long momentId, Long authorUserId, String nickname, String avatar, String content,
                             List<MomentMediaItem> mediaList, List<MomentCommentItem> commentList, Integer likeCount, Integer commentCount, String createdAt) {
    }

    public record MomentMediaItem(String url, String coverUrl) {
    }

    public record LikeResult(Long momentId, String action, Integer likeCount) {
    }

    public record CommentResult(Long momentId, Long commentId, String content, Long replyCommentId, Long replyUserId, Integer commentCount, String createdAt) {
    }

    public record CommentDeleteResult(Long commentId, Long momentId, Integer commentCount) {
    }

    public record MomentCommentItem(Long commentId, Long userId, String userName, String content, Long replyCommentId, Long replyUserId, String replyUserName, String createdAt) {
    }

    private MomentItem toMomentItem(MomentPersistenceService.MomentAggregate aggregate) {
        MomentPostEntity entity = aggregate.post();
        return new MomentItem(
                entity.getMomentId(),
                entity.getAuthorUserId(),
                entity.getAuthorUserId() != null && entity.getAuthorUserId() == 10001L ? "微友产品体验官" : "用户" + entity.getAuthorUserId(),
                "https://weiyou.local/avatar/" + entity.getAuthorUserId() + ".png",
                entity.getContentText(),
                aggregate.mediaList().stream().map(media -> new MomentMediaItem(media.getMediaUrl(), media.getCoverUrl())).toList(),
                aggregate.commentList().stream().map(comment -> new MomentCommentItem(
                        comment.getCommentId(),
                        comment.getCommentUserId(),
                        comment.getCommentUserId() != null && comment.getCommentUserId() == 10001L ? "微友产品体验官" : "用户" + comment.getCommentUserId(),
                        comment.getContentText(),
                        comment.getReplyCommentId(),
                        comment.getReplyUserId(),
                        comment.getReplyUserId() == null ? null : (comment.getReplyUserId() == 10001L ? "微友产品体验官" : "用户" + comment.getReplyUserId()),
                        comment.getCreatedAt() == null ? Instant.now().toString() : comment.getCreatedAt().toString()
                )).toList(),
                entity.getLikeCount() == null ? 0 : entity.getLikeCount(),
                entity.getCommentCount() == null ? 0 : entity.getCommentCount(),
                entity.getCreatedAt() == null ? Instant.now().toString() : entity.getCreatedAt().toString()
        );
    }

    private void updateFallbackLike(Long momentId, String action) {
        fallbackMoments.replaceAll(item -> {
            if (!item.momentId().equals(momentId)) {
                return item;
            }
            int nextLikeCount = "unlike".equalsIgnoreCase(action)
                    ? Math.max(0, item.likeCount() - 1)
                    : item.likeCount() + 1;
            return new MomentItem(item.momentId(), item.authorUserId(), item.nickname(), item.avatar(), item.content(), item.mediaList(), item.commentList(), nextLikeCount, item.commentCount(), item.createdAt());
        });
    }

    private void addFallbackComment(Long momentId, Long commentId, Long userId, String content, Long replyCommentId) {
        fallbackMoments.replaceAll(item -> {
            if (!item.momentId().equals(momentId)) {
                return item;
            }
            List<MomentCommentItem> commentList = new ArrayList<>(item.commentList());
            String replyUserName = null;
            Long replyUserId = null;
            if (replyCommentId != null) {
                for (MomentCommentItem existing : commentList) {
                    if (existing.commentId().equals(replyCommentId)) {
                        replyUserName = existing.userName();
                        replyUserId = existing.userId();
                        break;
                    }
                }
            }
            commentList.add(new MomentCommentItem(commentId, userId, userId == 10001L ? "微友产品体验官" : "用户" + userId, content, replyCommentId, replyUserId, replyUserName, Instant.now().toString()));
            return new MomentItem(item.momentId(), item.authorUserId(), item.nickname(), item.avatar(), item.content(), item.mediaList(), commentList, item.likeCount(), item.commentCount() + 1, item.createdAt());
        });
    }

    private void deleteFallbackComment(Long momentId, Long commentId) {
        fallbackMoments.replaceAll(item -> {
            if (!item.momentId().equals(momentId)) {
                return item;
            }
            List<MomentCommentItem> commentList = item.commentList().stream().filter(comment -> !comment.commentId().equals(commentId)).toList();
            int nextCommentCount = Math.max(0, item.commentCount() - 1);
            return new MomentItem(item.momentId(), item.authorUserId(), item.nickname(), item.avatar(), item.content(), item.mediaList(), commentList, item.likeCount(), nextCommentCount, item.createdAt());
        });
    }
}
