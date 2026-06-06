package com.cyberblog.backend.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentVO {
    private Long id;
    private Long articleId;
    private Long userId;
    private String username;
    private String avatarUrl;
    private Long parentId;
    private String content;
    private Integer likeCount;
    private Boolean liked;
    private LocalDateTime createdAt;
    private List<CommentVO> replies;
}
