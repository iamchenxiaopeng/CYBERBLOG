package com.cyberblog.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("articles")
public class Article {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    @TableField(updateStrategy = FieldStrategy.IGNORED)
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
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
