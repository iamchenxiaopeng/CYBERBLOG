package com.cyberblog.backend.service;

import com.cyberblog.backend.dto.LoginDTO;
import com.cyberblog.backend.dto.RegisterDTO;
import com.cyberblog.backend.vo.LoginVO;
import com.cyberblog.backend.vo.UserVO;

public interface UserService {
    LoginVO register(RegisterDTO dto);
    LoginVO login(LoginDTO dto);
    UserVO getById(Long id);
    UserVO updateProfile(Long userId, String bio, String avatarUrl);
}
