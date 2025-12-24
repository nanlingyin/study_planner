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
-- ===== Forum tables (drop first) =====
DROP TABLE IF EXISTS `forum_answer_collect`;
DROP TABLE IF EXISTS `forum_comment_vote`;
DROP TABLE IF EXISTS `forum_answer_vote`;
DROP TABLE IF EXISTS `forum_user_follow`;
DROP TABLE IF EXISTS `forum_topic_follow`;
DROP TABLE IF EXISTS `forum_question_follow`;
DROP TABLE IF EXISTS `forum_question_topic`;
DROP TABLE IF EXISTS `forum_comment`;
DROP TABLE IF EXISTS `forum_answer`;
DROP TABLE IF EXISTS `forum_question`;
DROP TABLE IF EXISTS `forum_topic`;
DROP TABLE IF EXISTS `user_settings`;
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
    `bio` VARCHAR(255) DEFAULT NULL COMMENT '个人简介',
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
-- 6. 用户设置表 (user_settings)
-- ============================================
CREATE TABLE `user_settings` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '设置ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `inactive_minutes` INT DEFAULT 4320 COMMENT '闲置时间阈值（分钟），超过此时间未打卡算闲置，默认3天',
    `reminder_interval_minutes` INT DEFAULT 720 COMMENT '提醒间隔（分钟），闲置后每隔多久提醒一次，默认12小时',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    CONSTRAINT `fk_settings_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户设置表';

-- ============================================
-- 7. 论坛系统表 (forum) - 最小可用版
-- 对齐前端：发帖(question)、话题(topic)、回答(answer)、评论/回复(comment)
-- ============================================

-- 7.1 话题表
CREATE TABLE `forum_topic` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '话题ID',
    `name` VARCHAR(50) NOT NULL COMMENT '话题名称',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '话题描述',
    `follow_count` INT NOT NULL DEFAULT 0 COMMENT '关注数（可冗余，可后续用SQL统计校准）',
    `question_count` INT NOT NULL DEFAULT 0 COMMENT '问题数（可冗余）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_topic_name` (`name`),
    KEY `idx_topic_follow_count` (`follow_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='论坛话题表';

-- 7.2 问题/帖子表（前端叫 question，本质发帖）
CREATE TABLE `forum_question` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '问题ID',
    `author_id` BIGINT NOT NULL COMMENT '作者用户ID',
    `title` VARCHAR(200) NOT NULL COMMENT '标题',
    `content` TEXT NOT NULL COMMENT '正文内容',
    `anonymous` TINYINT NOT NULL DEFAULT 0 COMMENT '是否匿名(0否/1是)',
    `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览数',
    `answer_count` INT NOT NULL DEFAULT 0 COMMENT '回答数（可冗余）',
    `follow_count` INT NOT NULL DEFAULT 0 COMMENT '关注数（可冗余）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_question_author` (`author_id`),
    KEY `idx_question_create_time` (`create_time`),
    FULLTEXT KEY `ft_question_title_content` (`title`, `content`),
    CONSTRAINT `fk_forum_question_user` FOREIGN KEY (`author_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='论坛问题/帖子表';

-- 7.3 问题-话题关联表（发帖时提交 topic_ids）
CREATE TABLE `forum_question_topic` (
    `question_id` BIGINT NOT NULL COMMENT '问题ID',
    `topic_id` BIGINT NOT NULL COMMENT '话题ID',
    PRIMARY KEY (`question_id`, `topic_id`),
    KEY `idx_qt_topic` (`topic_id`),
    CONSTRAINT `fk_qt_question` FOREIGN KEY (`question_id`) REFERENCES `forum_question` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_qt_topic` FOREIGN KEY (`topic_id`) REFERENCES `forum_topic` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问题-话题关联表';

-- 7.4 回答表（前端叫 answer）
CREATE TABLE `forum_answer` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '回答ID',
    `question_id` BIGINT NOT NULL COMMENT '问题ID',
    `author_id` BIGINT NOT NULL COMMENT '作者用户ID',
    `content` TEXT NOT NULL COMMENT '回答内容',
    `vote_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数（可冗余）',
    `comment_count` INT NOT NULL DEFAULT 0 COMMENT '评论数（可冗余）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_answer_question` (`question_id`),
    KEY `idx_answer_author` (`author_id`),
    KEY `idx_answer_create_time` (`create_time`),
    FULLTEXT KEY `ft_answer_content` (`content`),
    CONSTRAINT `fk_forum_answer_question` FOREIGN KEY (`question_id`) REFERENCES `forum_question` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_forum_answer_user` FOREIGN KEY (`author_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='论坛回答表';

-- 7.5 评论/回复表（前端 comment + replies：用 parent_id 表达“回复评论”）
CREATE TABLE `forum_comment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `answer_id` BIGINT NOT NULL COMMENT '回答ID',
    `author_id` BIGINT NOT NULL COMMENT '作者用户ID',
    `parent_id` BIGINT DEFAULT NULL COMMENT '父评论ID（为空表示评论回答；不为空表示回复评论）',
    `content` TEXT NOT NULL COMMENT '评论内容',
    `vote_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数（可冗余）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_comment_answer` (`answer_id`),
    KEY `idx_comment_parent` (`parent_id`),
    KEY `idx_comment_author` (`author_id`),
    CONSTRAINT `fk_forum_comment_answer` FOREIGN KEY (`answer_id`) REFERENCES `forum_answer` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_forum_comment_user` FOREIGN KEY (`author_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_forum_comment_parent` FOREIGN KEY (`parent_id`) REFERENCES `forum_comment` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='论坛评论/回复表';

-- ============================================
-- 7.6 交互表（前端已有调用：关注问题/关注话题/关注用户/点赞/收藏）
-- ============================================

-- 关注问题
CREATE TABLE `forum_question_follow` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `question_id` BIGINT NOT NULL COMMENT '问题ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_question` (`user_id`, `question_id`),
    KEY `idx_qf_question` (`question_id`),
    CONSTRAINT `fk_qf_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_qf_question` FOREIGN KEY (`question_id`) REFERENCES `forum_question` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问题关注表';

-- 关注话题
CREATE TABLE `forum_topic_follow` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `topic_id` BIGINT NOT NULL COMMENT '话题ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_topic` (`user_id`, `topic_id`),
    KEY `idx_tf_topic` (`topic_id`),
    CONSTRAINT `fk_tf_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_tf_topic` FOREIGN KEY (`topic_id`) REFERENCES `forum_topic` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='话题关注表';

-- 关注用户（用于 /forum/user/{id}/follow）
CREATE TABLE `forum_user_follow` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `follower_id` BIGINT NOT NULL COMMENT '关注者用户ID',
    `followee_id` BIGINT NOT NULL COMMENT '被关注用户ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_follower_followee` (`follower_id`, `followee_id`),
    KEY `idx_uf_followee` (`followee_id`),
    CONSTRAINT `fk_uf_follower` FOREIGN KEY (`follower_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_uf_followee` FOREIGN KEY (`followee_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户关注表';

-- 点赞回答
CREATE TABLE `forum_answer_vote` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `answer_id` BIGINT NOT NULL COMMENT '回答ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_answer` (`user_id`, `answer_id`),
    KEY `idx_av_answer` (`answer_id`),
    CONSTRAINT `fk_av_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_av_answer` FOREIGN KEY (`answer_id`) REFERENCES `forum_answer` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='回答点赞表';

-- 点赞评论
CREATE TABLE `forum_comment_vote` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `comment_id` BIGINT NOT NULL COMMENT '评论ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_comment` (`user_id`, `comment_id`),
    KEY `idx_cv_comment` (`comment_id`),
    CONSTRAINT `fk_cv_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_cv_comment` FOREIGN KEY (`comment_id`) REFERENCES `forum_comment` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论点赞表';

-- 收藏回答（前端 collect 是针对 answer）
CREATE TABLE `forum_answer_collect` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `answer_id` BIGINT NOT NULL COMMENT '回答ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_collect_answer` (`user_id`, `answer_id`),
    KEY `idx_ac_answer` (`answer_id`),
    CONSTRAINT `fk_ac_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_ac_answer` FOREIGN KEY (`answer_id`) REFERENCES `forum_answer` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='回答收藏表';

-- ============================================
-- 7.7 可选：插入一些初始话题（用于 /api/forum/topic/hot）
-- ============================================
INSERT INTO `forum_topic` (`name`, `description`) VALUES
('学习方法', '学习技巧与经验分享'),
('Python', 'Python 编程与项目实践'),
('Java', 'Java 后端与框架相关'),
('算法', '数据结构与算法题讨论')
ON DUPLICATE KEY UPDATE `description` = VALUES(`description`);


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
