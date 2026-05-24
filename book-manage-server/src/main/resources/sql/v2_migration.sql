-- V2.0 迁移脚本
USE library_db;

-- 添加图书价格字段（罚款上限使用）
ALTER TABLE `book` ADD COLUMN `price` DECIMAL(10,2) DEFAULT NULL COMMENT '图书价格(罚款上限)' AFTER `location`;

-- 为已有图书设置默认价格
UPDATE `book` SET `price` = 50.00 WHERE `price` IS NULL;

-- 添加借阅记录上次提醒时间字段（逾期每7天提醒一次）
ALTER TABLE `borrow_record` ADD COLUMN `last_remind_time` DATETIME DEFAULT NULL COMMENT '上次逾期提醒时间' AFTER `return_operator`;
