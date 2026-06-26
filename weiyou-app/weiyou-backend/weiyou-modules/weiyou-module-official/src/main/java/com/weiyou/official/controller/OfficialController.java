package com.weiyou.official.controller;

import com.weiyou.common.core.api.ApiResponse;
import com.weiyou.common.core.api.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
@RequestMapping("/official")
public class OfficialController {

    private final Map<Long, Integer> articleLikeCounter = new ConcurrentHashMap<>();
    private final Map<Long, Boolean> followState = new ConcurrentHashMap<>();

    @GetMapping("/account/list")
    public ApiResponse<List<OfficialAccountDetail>> accountList(@RequestParam(required = false) String keyword) {
        List<OfficialAccountDetail> list = List.of(
                new OfficialAccountDetail(20001L, "微友服务号", "https://weiyou.local/official/service.png",
                        "微友官方服务、活动与消息通知入口", true, followed(20001L)),
                new OfficialAccountDetail(20002L, "微友通知助手", "https://weiyou.local/official/notice.png",
                        "系统消息、版本更新与安全提醒", true, followed(20002L))
        );
        if (keyword == null || keyword.isBlank()) {
            return ApiResponse.ok(list);
        }
        String text = keyword.trim().toLowerCase();
        return ApiResponse.ok(list.stream().filter(item -> item.name().toLowerCase().contains(text)).toList());
    }

    @GetMapping("/account/detail")
    public ApiResponse<OfficialAccountDetail> getDetail(@RequestParam Long officialId) {
        return ApiResponse.ok(new OfficialAccountDetail(officialId, "微友服务号", "https://weiyou.local/official/service.png",
                "微友官方服务、活动与消息通知入口", true, followed(officialId)));
    }

    @GetMapping("/article/history")
    public ApiResponse<PageResponse<ArticleItem>> history(@RequestParam Long officialId,
                                                          @RequestParam(required = false) String cursor) {
        List<ArticleItem> list = List.of(
                new ArticleItem(300001L, officialId, "微友版本更新周报", "本周完成登录、聊天、钱包与朋友圈主链路联调。",
                        "https://weiyou.local/official/article-cover-1.png", Instant.now().minusSeconds(3600).toString(), likeCount(300001L)),
                new ArticleItem(300002L, officialId, "微友功能中心上线说明", "功能中心已接入公众号、小程序和钱包入口。",
                        "https://weiyou.local/official/article-cover-2.png", Instant.now().minusSeconds(7200).toString(), likeCount(300002L))
        );
        return ApiResponse.ok(PageResponse.of(list, 1, 20, list.size(), false, null));
    }

    @GetMapping("/article/detail")
    public ApiResponse<ArticleDetail> articleDetail(@RequestParam Long articleId) {
        return ApiResponse.ok(new ArticleDetail(
                articleId,
                20001L,
                "微友版本更新周报",
                "微友后端和 uni-app 前端已经打通登录、聊天、钱包、朋友圈、功能中心等核心链路。",
                "<p>本周完成了登录态、HTTP 联调、WebSocket 实时消息、钱包转账与红包、朋友圈发布等重点能力。</p><p>下一步将继续补齐公众号历史消息、小程序最近使用、Docker Compose 一键启动等能力。</p>",
                "https://weiyou.local/official/article-cover-1.png",
                Instant.now().minusSeconds(3600).toString(),
                likeCount(articleId)
        ));
    }

    @PostMapping("/article/like")
    public ApiResponse<ArticleLikeResult> likeArticle(@Valid @RequestBody ArticleLikeRequest request) {
        int current = articleLikeCounter.compute(request.articleId(), (key, oldValue) -> {
            int base = oldValue == null ? defaultLikeCount(key) : oldValue;
            if ("unlike".equalsIgnoreCase(request.action())) {
                return Math.max(0, base - 1);
            }
            return base + 1;
        });
        return ApiResponse.ok(new ArticleLikeResult(request.articleId(), request.action(), current));
    }

    @PostMapping("/account/follow")
    public ApiResponse<OfficialFollowResult> followAccount(@Valid @RequestBody OfficialFollowRequest request) {
        boolean followed = followState.compute(request.officialId(), (key, oldValue) -> {
            if ("unfollow".equalsIgnoreCase(request.action())) {
                return false;
            }
            return true;
        });
        return ApiResponse.ok(new OfficialFollowResult(request.officialId(), request.action(), followed));
    }

    public record OfficialAccountDetail(Long officialId, String name, String avatar, String intro,
                                        Boolean verified, Boolean followed) {
    }

    public record ArticleItem(Long articleId, Long officialId, String title, String summary, String cover, String publishAt, Integer likeCount) {
    }

    public record ArticleDetail(Long articleId, Long officialId, String title, String summary, String contentHtml,
                                String cover, String publishAt, Integer likeCount) {
    }

    public record ArticleLikeRequest(@NotNull Long articleId, @NotBlank String action) {
    }

    public record ArticleLikeResult(Long articleId, String action, Integer likeCount) {
    }

    public record OfficialFollowRequest(@NotNull Long officialId, @NotBlank String action) {
    }

    public record OfficialFollowResult(Long officialId, String action, Boolean followed) {
    }

    private int likeCount(Long articleId) {
        return articleLikeCounter.computeIfAbsent(articleId, this::defaultLikeCount);
    }

    private boolean followed(Long officialId) {
        return followState.computeIfAbsent(officialId, key -> true);
    }

    private int defaultLikeCount(Long articleId) {
        return articleId != null && articleId == 300002L ? 9 : 18;
    }
}
