-- Update admin password with known BCrypt hash for "admin"
UPDATE im_user SET password = '$2a$10$N9qo8uLOickgx2ZMRZkOXePWwAhS8hPv0pQ5oqGoPGy5tIUPO/LIa' WHERE username = 'admin';
SELECT id, username, LENGTH(password) as pwd_length FROM im_user WHERE username = 'admin';
