package com.cyberblog.backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cyberblog.backend.common.Result;
import com.cyberblog.backend.dto.ArticleDTO;
import com.cyberblog.backend.service.ArticleService;
import com.cyberblog.backend.vo.ArticleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "文章管理")
@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @Operation(summary = "获取文章列表（支持关键词搜索）")
    @GetMapping
    public Result<IPage<ArticleVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            Authentication auth) {
        Long userId = auth != null ? (Long) auth.getPrincipal() : null;
        return Result.ok(articleService.listArticles(page, size, keyword, userId));
    }

    @Operation(summary = "获取文章详情")
    @GetMapping("/{id}")
    public Result<ArticleVO> get(@PathVariable Long id, Authentication auth) {
        Long userId = auth != null ? (Long) auth.getPrincipal() : null;
        return Result.ok(articleService.getArticle(id, userId));
    }

    @Operation(summary = "手写发布文章")
    @PostMapping
    public Result<ArticleVO> create(@Valid @RequestBody ArticleDTO dto, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.ok(articleService.createArticle(dto, userId));
    }

    @Operation(summary = "上传 Markdown 文件发布")
    @PostMapping("/upload")
    public Result<ArticleVO> uploadMd(
            @RequestParam MultipartFile file,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) String coverImg,
            Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.ok(articleService.uploadMarkdown(file, title, tags, coverImg, userId));
    }

    @Operation(summary = "更新文章")
    @PutMapping("/{id}")
    public Result<ArticleVO> update(@PathVariable Long id,
                                     @RequestBody ArticleDTO dto,
                                     Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.ok(articleService.updateArticle(id, dto, userId));
    }

    @Operation(summary = "删除文章")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        articleService.deleteArticle(id, userId);
        return Result.ok();
    }
}
