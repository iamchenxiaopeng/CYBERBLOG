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

    @Operation(summary = "注册（一键注册，注册即登录）")
    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody RegisterDTO dto) {
        return Result.ok(userService.register(dto));
    }

    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
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
}
