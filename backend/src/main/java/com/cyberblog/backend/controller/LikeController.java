package com.cyberblog.backend.controller;

import com.cyberblog.backend.common.Result;
import com.cyberblog.backend.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "点赞")
@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @Operation(summary = "点赞/取消点赞 (targetType: article | comment)。登录用户传 Authorization，未登录传 guestId")
    @PostMapping("/{targetType}/{targetId}")
    public Result<?> toggle(@PathVariable String targetType,
                              @PathVariable Long targetId,
                              @RequestHeader(value = "X-Guest-Id", required = false) String guestId,
                              Authentication auth) {
        Long userId = auth != null ? (Long) auth.getPrincipal() : null;
        boolean liked = likeService.toggleLike(userId, guestId, targetId, targetType);
        return Result.ok(java.util.Map.of("liked", liked));
    }
}
