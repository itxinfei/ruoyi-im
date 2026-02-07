-- 为im_message表的content字段添加全文索引
-- 注意：MySQL 5.7.6+ 支持中文分词ngram解析器

-- 添加全文索引（支持中文）
ALTER TABLE `im_message` ADD FULLTEXT INDEX `ft_content_cn` (`content`) WITH PARSER ngram;

-- 如果数据库不支持ngram，使用普通全文索引
-- ALTER TABLE `im_message` ADD FULLTEXT INDEX `ft_content` (`content`);
