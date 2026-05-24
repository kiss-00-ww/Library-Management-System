-- 首先检查admin用户是否存在
SELECT id, username, password, role FROM user WHERE username = 'admin';

-- 如果admin用户不存在或密码不正确，执行以下SQL来更新
-- 密码 "admin123" 的BCrypt哈希
UPDATE user
SET password = '$2a$10$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4tNlAHh.lSq.fZ7.'
WHERE username = 'admin';

-- 再次验证
SELECT id, username, password, role FROM user WHERE username = 'admin';