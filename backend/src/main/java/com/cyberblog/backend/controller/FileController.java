package com.cyberblog.backend.controller;

import com.cyberblog.backend.common.Result;
import com.cyberblog.backend.service.MinioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Tag(name = "文件上传")
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final MinioService minioService;

    @Operation(summary = "上传图片（用于封面/头像等）")
    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(@RequestParam MultipartFile file) {
        String url = minioService.upload(file, "images/");
        return Result.ok(Map.of("url", url));
    }
}
