package com.cyberblog.backend.vo;

import lombok.Data;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String email;
    private String avatarUrl;
    private String bio;
}
