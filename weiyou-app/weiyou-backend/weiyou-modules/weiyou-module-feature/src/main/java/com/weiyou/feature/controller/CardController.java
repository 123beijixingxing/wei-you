package com.weiyou.feature.controller;

import com.weiyou.common.core.api.ApiResponse;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/card")
public class CardController {

    private final CopyOnWriteArrayList<CardItem> cards = new CopyOnWriteArrayList<>(List.of(
            new CardItem(800001L, "coupon", "咖啡 8 折券", "微友咖啡馆", "2026-12-31", "未使用"),
            new CardItem(800002L, "member", "微友会员卡", "微友服务", "长期有效", "已激活"),
            new CardItem(800003L, "transport", "城市通行码", "杭州地铁", Instant.now().plusSeconds(86400).toString(), "可使用")
    ));

    @GetMapping("/list")
    public ApiResponse<List<CardItem>> list(@RequestParam(required = false) String cardType) {
        List<CardItem> list = List.copyOf(cards);
        if (cardType == null || cardType.isBlank()) {
            return ApiResponse.ok(list);
        }
        return ApiResponse.ok(list.stream().filter(item -> item.cardType().equalsIgnoreCase(cardType)).toList());
    }

    @PostMapping("/use")
    public ApiResponse<CardItem> use(@RequestBody CardActionRequest request) {
        CardItem updated = cards.stream()
                .filter(item -> item.cardId().equals(request.cardId()))
                .findFirst()
                .map(item -> new CardItem(
                        item.cardId(),
                        item.cardType(),
                        item.title(),
                        item.provider(),
                        item.expireText(),
                        "已使用"
                ))
                .orElse(null);
        if (updated != null) {
            cards.removeIf(item -> item.cardId().equals(request.cardId()));
            cards.add(0, updated);
        }
        return ApiResponse.ok(updated);
    }

    public record CardItem(Long cardId, String cardType, String title, String provider, String expireText, String status) {
    }

    public record CardActionRequest(Long cardId) {
    }
}
