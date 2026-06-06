package com.cyberblog.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ArticleDTO {
    @NotBlank(message = "标题不能为空")
    private String title;
    private String content;
    private String contentType = "markdown";
    private String coverImg;
    private String summary;
    private String tags;
    private String status = "published";
}
