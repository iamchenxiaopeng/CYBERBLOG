package com.cyberblog.backend.controller;

import com.cyberblog.backend.common.Result;
import com.cyberblog.backend.dto.LoginDTO;
import com.cyberblog.backend.dto.RegisterDTO;
import com.cyberblog.backend.service.UserService;
import com.cyberblog.backend.vo.LoginVO;
import com.cyberblog.backend.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户认证")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    // 请求时效窗口：5 分钟（防重放攻击）
    private static final long TIMESTAMP_WINDOW_SECONDS = 300;

    @Operation(summary = "注册（一键注册，注册即登录）")
    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody RegisterDTO dto) {
        validateTimestamp(dto.getTimestamp());
        return Result.ok(userService.register(dto));
    }

    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        validateTimestamp(dto.getTimestamp());
        return Result.ok(userService.login(dto));
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public Result<UserVO> me(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.ok(userService.getById(userId));
    }

    @Operation(summary = "更新个人资料")
    @PutMapping("/profile")
    public Result<UserVO> updateProfile(@RequestParam(required = false) String bio,
                                         @RequestParam(required = false) String avatarUrl,
                                         Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.ok(userService.updateProfile(userId, bio, avatarUrl));
    }

    /**
     * 验证 timestamp 是否在有效窗口内（±5 分钟）
     * 超时请求被拒绝，截获的哈希无法在 5 分钟后重放
     */
    private void validateTimestamp(Long timestamp) {
        if (timestamp == null) {
            throw new IllegalArgumentException("请求时间戳不能为空");
        }
        long now = System.currentTimeMillis() / 1000;
        long diff = Math.abs(now - timestamp);
        if (diff > TIMESTAMP_WINDOW_SECONDS) {
            throw new IllegalArgumentException("请求已过期，请刷新页面重试");
        }
    }
}
