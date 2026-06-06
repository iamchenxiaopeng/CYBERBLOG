package com.cyberblog.backend.controller;

import com.cyberblog.backend.common.Result;
import com.cyberblog.backend.dto.CommentDTO;
import com.cyberblog.backend.service.CommentService;
import com.cyberblog.backend.vo.CommentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "评论管理")
@RestController
@RequestMapping("/api/articles/{articleId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "获取文章评论列表")
    @GetMapping
    public Result<List<CommentVO>> list(@PathVariable Long articleId, Authentication auth) {
        Long userId = auth != null ? (Long) auth.getPrincipal() : null;
        return Result.ok(commentService.listByArticle(articleId, userId));
    }

    @Operation(summary = "发表评论")
    @PostMapping
    public Result<CommentVO> add(@PathVariable Long articleId,
                                  @Valid @RequestBody CommentDTO dto,
                                  Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.ok(commentService.addComment(articleId, dto, userId));
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/{commentId}")
    public Result<Void> delete(@PathVariable Long articleId,
                                @PathVariable Long commentId,
                                Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        commentService.deleteComment(commentId, userId);
        return Result.ok();
    }
}
