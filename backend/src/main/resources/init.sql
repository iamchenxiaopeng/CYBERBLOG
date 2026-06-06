-- cyberblog database initialization

CREATE DATABASE IF NOT EXISTS cyberblog DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cyberblog;

-- 用户表
CREATE TABLE IF NOT EXISTS `users` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL UNIQUE,
  `email` VARCHAR(100) UNIQUE,
  `password_hash` VARCHAR(255) NOT NULL,
  `avatar_url` VARCHAR(500) DEFAULT '',
  `bio` VARCHAR(500) DEFAULT '',
  `role` VARCHAR(20) DEFAULT 'user',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_username` (`username`),
  INDEX `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 文章表
CREATE TABLE IF NOT EXISTS `articles` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `title` VARCHAR(200) NOT NULL,
  `content` LONGTEXT,
  `content_type` VARCHAR(10) DEFAULT 'markdown',
  `file_url` VARCHAR(500) DEFAULT '',
  `cover_img` VARCHAR(500) DEFAULT '',
  `summary` VARCHAR(500) DEFAULT '',
  `tags` VARCHAR(300) DEFAULT '',
  `status` VARCHAR(10) DEFAULT 'published',
  `view_count` INT DEFAULT 0,
  `like_count` INT DEFAULT 0,
  `comment_count` INT DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 评论表
CREATE TABLE IF NOT EXISTS `comments` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `article_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `parent_id` BIGINT DEFAULT 0,
  `content` TEXT NOT NULL,
  `like_count` INT DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_article_id` (`article_id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 点赞表
CREATE TABLE IF NOT EXISTS `likes` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT DEFAULT NULL,
  `guest_id` VARCHAR(64) DEFAULT NULL,
  `target_id` BIGINT NOT NULL,
  `target_type` VARCHAR(20) NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_like_user` (`user_id`, `target_id`, `target_type`),
  UNIQUE KEY `uk_like_guest` (`guest_id`, `target_id`, `target_type`),
  INDEX `idx_target` (`target_id`, `target_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
