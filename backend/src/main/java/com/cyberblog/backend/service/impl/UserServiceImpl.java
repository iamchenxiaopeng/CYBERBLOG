package com.cyberblog.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyberblog.backend.common.BusinessException;
import com.cyberblog.backend.dto.LoginDTO;
import com.cyberblog.backend.dto.RegisterDTO;
import com.cyberblog.backend.entity.User;
import com.cyberblog.backend.mapper.UserMapper;
import com.cyberblog.backend.security.JwtUtil;
import com.cyberblog.backend.service.UserService;
import com.cyberblog.backend.vo.LoginVO;
import com.cyberblog.backend.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public LoginVO register(RegisterDTO dto) {
        // check username unique
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
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
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("用户名或密码错误");
        }
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
