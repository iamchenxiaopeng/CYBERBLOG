-- 数据库迁移 V2：likes 表支持游客点赞
-- 执行方式：在 MySQL 中运行，或 docker exec 进入容器执行

USE cyberblog;

-- 1. 添加 guest_id 列
ALTER TABLE `likes` ADD COLUMN `guest_id` VARCHAR(64) DEFAULT NULL AFTER `user_id`;

-- 2. 删除旧唯一索引（名为 uk_like）
-- 若报错 "check that column/key exists"，先执行：SHOW INDEX FROM likes; 查看实际索引名
ALTER TABLE `likes` DROP INDEX `uk_like`;

-- 3. 新建两个唯一索引
ALTER TABLE `likes` ADD UNIQUE KEY `uk_like_user` (`user_id`, `target_id`, `target_type`);
ALTER TABLE `likes` ADD UNIQUE KEY `uk_like_guest` (`guest_id`, `target_id`, `target_type`);

-- 4. user_id 改为允许 NULL
ALTER TABLE `likes` MODIFY COLUMN `user_id` BIGINT DEFAULT NULL;

-- 验证结果
SHOW CREATE TABLE `likes`;
