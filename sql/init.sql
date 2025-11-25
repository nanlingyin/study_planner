-- ============================================
-- 智能学习计划生成器 - 数据库初始化脚本
-- Smart Study Planner Database Init Script
-- 创建日期: 2025-11-25
-- ============================================

-- 设置字符编码（解决中文乱码问题）
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET character_set_connection = utf8mb4;
SET character_set_results = utf8mb4;
SET character_set_client = utf8mb4;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS study_planner 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_unicode_ci;

USE study_planner;

-- 设置当前数据库的字符集
SET NAMES utf8mb4;

-- ============================================
-- 删除已存在的表（注意顺序：先删除有外键依赖的子表）
-- ============================================
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `chat_history`;
DROP TABLE IF EXISTS `check_in`;
DROP TABLE IF EXISTS `plan_detail`;
DROP TABLE IF EXISTS `study_plan`;
DROP TABLE IF EXISTS `user`;

DROP VIEW IF EXISTS `v_user_study_stats`;
DROP VIEW IF EXISTS `v_plan_progress`;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 1. 用户表 (user)
-- ============================================
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 2. 学习计划表 (study_plan)
-- ============================================
CREATE TABLE `study_plan` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '计划ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `title` VARCHAR(100) NOT NULL COMMENT '计划标题',
    `goal` TEXT NOT NULL COMMENT '学习目标描述',
    `level` VARCHAR(20) DEFAULT '零基础' COMMENT '基础水平(零基础/初级/中级/高级)',
    `daily_hours` DECIMAL(3,1) DEFAULT 2.0 COMMENT '每日学习时长(小时)',
    `total_days` INT DEFAULT 30 COMMENT '计划总天数',
    `start_date` DATE NOT NULL COMMENT '开始日期',
    `end_date` DATE NOT NULL COMMENT '结束日期',
    `status` VARCHAR(20) DEFAULT '进行中' COMMENT '状态(进行中/已完成/已放弃)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    CONSTRAINT `fk_plan_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习计划表';

-- ============================================
-- 3. 计划详情表 (plan_detail) - 每日任务
-- ============================================
CREATE TABLE `plan_detail` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '详情ID',
    `plan_id` BIGINT NOT NULL COMMENT '计划ID',
    `day_number` INT NOT NULL COMMENT '第几天',
    `content` TEXT NOT NULL COMMENT '学习内容',
    `duration` DECIMAL(3,1) DEFAULT 2.0 COMMENT '预计时长(小时)',
    `resources` TEXT DEFAULT NULL COMMENT '推荐资源(JSON格式)',
    `is_completed` TINYINT DEFAULT 0 COMMENT '是否完成(0-未完成/1-已完成)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_plan_id` (`plan_id`),
    KEY `idx_day_number` (`day_number`),
    CONSTRAINT `fk_detail_plan` FOREIGN KEY (`plan_id`) REFERENCES `study_plan` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='计划详情表（每日任务）';

-- ============================================
-- 4. 打卡记录表 (check_in)
-- ============================================
CREATE TABLE `check_in` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '打卡ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `plan_id` BIGINT NOT NULL COMMENT '计划ID',
    `detail_id` BIGINT NOT NULL COMMENT '任务详情ID',
    `check_date` DATE NOT NULL COMMENT '打卡日期',
    `study_hours` DECIMAL(3,1) DEFAULT NULL COMMENT '实际学习时长(小时)',
    `note` TEXT DEFAULT NULL COMMENT '学习心得',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_plan_id` (`plan_id`),
    KEY `idx_check_date` (`check_date`),
    UNIQUE KEY `uk_user_date_detail` (`user_id`, `check_date`, `detail_id`),
    CONSTRAINT `fk_checkin_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_checkin_plan` FOREIGN KEY (`plan_id`) REFERENCES `study_plan` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_checkin_detail` FOREIGN KEY (`detail_id`) REFERENCES `plan_detail` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='打卡记录表';

-- ============================================
-- 5. AI对话记录表 (chat_history)
-- ============================================
CREATE TABLE `chat_history` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `question` TEXT NOT NULL COMMENT '用户问题',
    `answer` TEXT NOT NULL COMMENT 'AI回答',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    CONSTRAINT `fk_chat_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI对话记录表';

-- ============================================
-- 插入测试数据
-- ============================================

-- 插入测试用户 (密码为 123456 的BCrypt加密)
INSERT INTO `user` (`username`, `password`, `email`) VALUES 
('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'test@example.com'),
('demo', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'demo@example.com');

-- 插入示例学习计划
INSERT INTO `study_plan` (`user_id`, `title`, `goal`, `level`, `daily_hours`, `total_days`, `start_date`, `end_date`, `status`) VALUES 
(1, 'Python入门30天', '从零开始学习Python编程，掌握基础语法和常用库', '零基础', 2.0, 30, '2025-11-25', '2025-12-24', '进行中');

-- 插入示例每日任务
INSERT INTO `plan_detail` (`plan_id`, `day_number`, `content`, `duration`, `resources`) VALUES 
(1, 1, '了解Python简介，安装Python环境，学习print函数', 2.0, '{"books": ["Python编程从入门到实践"], "videos": ["B站小甲鱼Python教程"]}'),
(1, 2, '学习变量、数据类型（整数、浮点数、字符串）', 2.0, '{"books": ["Python编程从入门到实践"], "websites": ["菜鸟教程"]}'),
(1, 3, '学习运算符和表达式，完成简单计算练习', 2.0, NULL),
(1, 4, '学习条件语句if-elif-else', 2.0, NULL),
(1, 5, '学习循环语句for和while', 2.0, NULL);

-- ============================================
-- 创建视图（可选，方便查询）
-- ============================================

-- 用户学习统计视图
CREATE OR REPLACE VIEW `v_user_study_stats` AS
SELECT 
    u.id AS user_id,
    u.username,
    COUNT(DISTINCT sp.id) AS total_plans,
    COUNT(DISTINCT ci.id) AS total_checkins,
    COALESCE(SUM(ci.study_hours), 0) AS total_study_hours
FROM `user` u
LEFT JOIN `study_plan` sp ON u.id = sp.user_id
LEFT JOIN `check_in` ci ON u.id = ci.user_id
GROUP BY u.id, u.username;

-- 计划进度视图
CREATE OR REPLACE VIEW `v_plan_progress` AS
SELECT 
    sp.id AS plan_id,
    sp.title,
    sp.user_id,
    sp.total_days,
    COUNT(pd.id) AS total_tasks,
    SUM(pd.is_completed) AS completed_tasks,
    ROUND(SUM(pd.is_completed) / COUNT(pd.id) * 100, 2) AS progress_percent
FROM `study_plan` sp
LEFT JOIN `plan_detail` pd ON sp.id = pd.plan_id
GROUP BY sp.id, sp.title, sp.user_id, sp.total_days;

-- ============================================
-- 完成
-- ============================================
SELECT '数据库初始化完成！Database initialization completed!' AS message;
