package com.cyberblog.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyberblog.backend.common.BusinessException;
import com.cyberblog.backend.dto.LoginDTO;
import com.cyberblog.backend.dto.RegisterDTO;
import com.cyberblog.backend.entity.User;
import com.cyberblog.backend.mapper.UserMapper;
import com.cyberblog.backend.security.JwtUtil;
import com.cyberblog.backend.service.RateLimitService;
import com.cyberblog.backend.service.UserService;
import com.cyberblog.backend.vo.LoginVO;
import com.cyberblog.backend.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RateLimitService rateLimitService;

    @Override
    public LoginVO register(RegisterDTO dto) {
        // check username unique
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        // check email unique（仅当用户填写了真实邮箱时）
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            Long emailCount = userMapper.selectCount(
                    new LambdaQueryWrapper<User>().eq(User::getEmail, dto.getEmail()));
            if (emailCount > 0) {
                throw new BusinessException("邮箱已被注册");
            }
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        String email = (dto.getEmail() != null && !dto.getEmail().isBlank())
                ? dto.getEmail()
                : dto.getUsername() + "@cyberblog.local";
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setAvatarUrl("https://api.dicebear.com/7.x/pixel-art/svg?seed=" + dto.getUsername());
        user.setBio("这个人很懒，什么都没留下。");
        user.setRole("user");
        userMapper.insert(user);
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return new LoginVO(token, toVO(user));
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        // ── 暴力破解防护 ──
        // 检查用户名是否被锁定
        if (rateLimitService.isUserLocked(dto.getUsername())) {
            long remaining = rateLimitService.getUserLockRemainingSeconds(dto.getUsername());
            throw new BusinessException("登录失败次数过多，请 " + (remaining / 60 + 1) + " 分钟后再试");
        }

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            // 记录失败次数
            rateLimitService.recordLoginFailure(dto.getUsername());
            throw new BusinessException("用户名或密码错误");
        }

        // 登录成功 → 清除失败计数
        rateLimitService.clearLoginFailure(dto.getUsername());
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return new LoginVO(token, toVO(user));
    }

    @Override
    public UserVO getById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException(404, "用户不存在");
        return toVO(user);
    }

    @Override
    public UserVO updateProfile(Long userId, String bio, String avatarUrl) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(404, "用户不存在");
        if (bio != null) user.setBio(bio);
        if (avatarUrl != null) user.setAvatarUrl(avatarUrl);
        userMapper.updateById(user);
        return toVO(user);
    }

    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setBio(user.getBio());
        return vo;
    }
}
