package com.weiyou.feedback.controller;

import com.weiyou.common.core.api.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    @PostMapping("/create")
    public ApiResponse<Void> create(@Valid @RequestBody FeedbackCreateRequest request) {
        return ApiResponse.ok();
    }

    public record FeedbackCreateRequest(@NotBlank String content, List<String> images, String contact) {
    }
}
