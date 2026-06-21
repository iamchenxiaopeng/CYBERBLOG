package com.cyberblog.backend.config;

import com.cyberblog.backend.entity.User;
import com.cyberblog.backend.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 数据初始化器：首次启动时自动创建默认 admin 账号
 * 密码存储格式：bcrypt(sha256(明文))
 * 与前端传输前加密格式保持一致
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "Admin@2077";

    @Override
    public void run(String... args) {
        try {
            initAdminUser();
        } catch (Exception e) {
            log.error("数据初始化失败: {}", e.getMessage(), e);
        }
    }

    private void initAdminUser() {
        // 密码格式：bcrypt(sha256(明文))，与前端传输前加密保持一致
        String sha256Password = sha256Hex(DEFAULT_ADMIN_PASSWORD);
        String bcryptHash = passwordEncoder.encode(sha256Password);

        User admin = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUsername, DEFAULT_ADMIN_USERNAME));

        if (admin == null) {
            log.info("正在创建默认管理员账号...");
            admin = new User();
            admin.setUsername(DEFAULT_ADMIN_USERNAME);
            admin.setEmail("admin@cyberblog.local");
            admin.setPasswordHash(bcryptHash);
            admin.setAvatarUrl("/avatar-default.svg");
            admin.setBio("赛博博客系统管理员");
            admin.setRole("admin");
            userMapper.insert(admin);
            log.info("默认管理员创建完成 — 用户名: {}", DEFAULT_ADMIN_USERNAME);
        } else {
            // 已存在 → 强制更新密码为 bcrypt(sha256(明文)) 格式
            admin.setPasswordHash(bcryptHash);
            if (admin.getRole() == null || !"admin".equals(admin.getRole())) {
                admin.setRole("admin");
            }
            userMapper.updateById(admin);
            log.info("管理员密码已更新为 bcrypt(sha256) 格式 — 用户名: {}", DEFAULT_ADMIN_USERNAME);
        }
        log.info("管理员初始化完成 — 用户名: {}, 密码: {} (bcrypt(sha256) 格式)",
                DEFAULT_ADMIN_USERNAME, DEFAULT_ADMIN_PASSWORD);
    }

    /**
     * 计算字符串的 SHA-256 hash，返回小写 hex 字符串
     * 与前端 crypto.ts 里的 sha256() 函数保持一致的输出格式
     */
    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b & 0xff));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
