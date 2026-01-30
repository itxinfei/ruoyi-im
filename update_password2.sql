-- Update admin password with correct 60-char BCrypt hash for "admin123"
UPDATE im_user SET password = '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG' WHERE username = 'admin';
SELECT id, username, LENGTH(password) as pwd_length, LEFT(password, 30) as password_prefix FROM im_user WHERE username = 'admin';
