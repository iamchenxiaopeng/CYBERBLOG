package com.cyberblog.backend.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ArticleVO {
    private Long id;
    private Long userId;
    private String username;
    private String avatarUrl;
    private String title;
    private String content;
    private String contentType;
    private String fileUrl;
    private String coverImg;
    private String summary;
    private String tags;
    private String status;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean liked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
