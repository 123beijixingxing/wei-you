package com.weiyou.search.controller;

import com.weiyou.common.core.api.ApiResponse;
import com.weiyou.common.core.api.PageResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search")
public class SearchController {

    @GetMapping("/suggest")
    public ApiResponse<List<String>> suggest(@RequestParam String keyword, @RequestParam(required = false) String bizType) {
        return ApiResponse.ok(List.of(keyword, keyword + " 朋友圈", keyword + " 小程序"));
    }

    @GetMapping("/global")
    public ApiResponse<PageResponse<SearchItem>> global(@RequestParam String keyword,
                                                        @RequestParam(required = false) String bizType,
                                                        @RequestParam(defaultValue = "1") int pageNo) {
        List<SearchItem> list = List.of(
                new SearchItem("contact", "10002", "阿泽", "通讯录联系人", "https://weiyou.local/avatar/10002.png", "/pages/contacts/profile?id=10002"),
                new SearchItem("official", "20001", "微友服务号", "公众号", "https://weiyou.local/official/service.png", "/pages/official/detail?officialId=20001")
        );
        return ApiResponse.ok(PageResponse.of(list, pageNo, 20, list.size(), false, null));
    }

    public record SearchItem(String bizType, String bizId, String title, String subtitle, String cover, String routePath) {
    }
}
