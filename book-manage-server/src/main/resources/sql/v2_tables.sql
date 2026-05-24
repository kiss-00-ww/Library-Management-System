-- V2.0 新增表 DDL
USE library_db;

-- 预约记录表
CREATE TABLE IF NOT EXISTS `reservation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `book_id` BIGINT NOT NULL,
  `reserve_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `expire_time` DATETIME DEFAULT NULL,
  `status` ENUM('WAITING','NOTIFIED','FULFILLED','CANCELLED','EXPIRED') NOT NULL DEFAULT 'WAITING',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_book` (`book_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_reservation_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_reservation_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 系统参数配置表
CREATE TABLE IF NOT EXISTS `system_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `config_key` VARCHAR(100) NOT NULL,
  `config_value` VARCHAR(500) NOT NULL,
  `description` VARCHAR(255) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 书评评分表
CREATE TABLE IF NOT EXISTS `book_review` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `book_id` BIGINT NOT NULL,
  `rating` TINYINT NOT NULL DEFAULT 5 COMMENT '评分1-5',
  `content` TEXT,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_book` (`book_id`),
  CONSTRAINT `fk_review_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_review_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 公告表
CREATE TABLE IF NOT EXISTS `announcement` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(200) NOT NULL,
  `content` TEXT NOT NULL,
  `type` ENUM('NOTICE','IMPORTANT','URGENT') NOT NULL DEFAULT 'NOTICE',
  `expire_time` DATETIME DEFAULT NULL,
  `create_user_id` BIGINT DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_create_user` (`create_user_id`),
  CONSTRAINT `fk_announcement_user` FOREIGN KEY (`create_user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 通知消息表
CREATE TABLE IF NOT EXISTS `notification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `title` VARCHAR(200) NOT NULL,
  `content` TEXT NOT NULL,
  `type` ENUM('SYSTEM','EMAIL','SMS') NOT NULL DEFAULT 'SYSTEM',
  `is_read` TINYINT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_is_read` (`is_read`),
  CONSTRAINT `fk_notification_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入默认系统配置（借阅规则参数化）
INSERT INTO `system_config` (`config_key`, `config_value`, `description`) VALUES
('borrow_days_student', '30', '学生借阅天数'),
('borrow_days_teacher', '60', '教师借阅天数'),
('max_borrow_count_student', '5', '学生最大借阅数量'),
('max_borrow_count_teacher', '10', '教师最大借阅数量'),
('renew_max_times', '1', '最大续借次数'),
('renew_days', '30', '续借天数'),
('renew_window_days', '7', '续借窗口期(到期前N天内可续借)'),
('fine_rate_per_day', '0.1', '每日逾期罚款金额(元)'),
('reserve_keep_days', '7', '预约保留天数')
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`);
