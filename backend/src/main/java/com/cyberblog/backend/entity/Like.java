package com.cyberblog.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("likes")
public class Like {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;       // 登录用户ID，未登录时为 null
    private String guestId;     // 未登录游客ID（UUID），登录时为 null
    private Long targetId;
    private String targetType;  // "article" | "comment"
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
