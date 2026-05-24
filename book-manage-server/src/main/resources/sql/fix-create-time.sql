-- 修复现有用户的注册时间（如果为NULL）
UPDATE user SET create_time = NOW() WHERE create_time IS NULL;

-- 如果需要重新初始化admin用户的注册时间
UPDATE user SET create_time = '2026-01-01 00:00:00' WHERE username = 'admin';

-- 验证结果
SELECT id, username, real_name, create_time FROM user WHERE username = 'admin';