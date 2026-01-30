-- Update admin password to known BCrypt hash for "admin123"
UPDATE im_user SET password = '$2a$10$N9qo8uLOickgx2ZMRZkOXjJwZsWd1rVZJZdKcV8gKp1XZgC' WHERE username = 'admin';
SELECT id, username, LEFT(password, 30) as password_prefix FROM im_user WHERE username = 'admin';
