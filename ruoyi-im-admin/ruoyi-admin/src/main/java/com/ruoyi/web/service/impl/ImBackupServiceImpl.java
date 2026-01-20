package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.ImBackup;
import com.ruoyi.web.service.ImBackupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * IM数据备份Service实现（Admin模块专用）
 */
@Service
public class ImBackupServiceImpl implements ImBackupService {

    private static final Logger logger = LoggerFactory.getLogger(ImBackupServiceImpl.class);

    /** 备份文件存储目录 */
    @Value("${backup.path:D:/ruoyi-im-backups}")
    private String backupPath;

    /** 数据库配置 */
    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    /** MySQL dump 命令路径 */
    private static final String MYSQLDUMP_CMD = "mysqldump";

    /** MySQL 命令路径 */
    private static final String MYSQL_CMD = "mysql";

    /**
     * 备份记录存储（实际项目中应使用数据库存储）
     */
    private static final Map<Long, ImBackup> BACKUP_STORE = new HashMap<>();
    private static long backupIdCounter = 1;

    @Override
    public List<ImBackup> selectBackupList(ImBackup backup) {
        return BACKUP_STORE.values().stream().collect(Collectors.toList());
    }

    @Override
    public ImBackup selectBackupById(Long id) {
        return BACKUP_STORE.get(id);
    }

    @Override
    @Transactional
    public ImBackup manualBackup(String description, String operator) {
        ImBackup backup = new ImBackup();
        backup.setId(backupIdCounter++);
        backup.setBackupType("MANUAL");
        backup.setStatus("IN_PROGRESS");
        backup.setProgress(0);
        backup.setOperator(operator);
        backup.setDescription(description);
        backup.setCreateTime(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));

        try {
            // 生成备份文件名
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "im_backup_" + timestamp + ".sql";
            String filePath = backupPath + File.separator + fileName;

            // 确保备份目录存在
            Path backupDir = Paths.get(backupPath);
            if (!Files.exists(backupDir)) {
                Files.createDirectories(backupDir);
            }

            backup.setFileName(fileName);
            backup.setFilePath(filePath);
            backup.setProgress(20);

            // 执行数据库备份
            executeBackup(filePath, backup);

            // 获取文件大小
            File file = new File(filePath);
            backup.setFileSize(file.length());

            // 更新备份状态
            backup.setStatus("SUCCESS");
            backup.setProgress(100);
            backup.setCompletedTime(LocalDateTime.now());

            logger.info("数据备份成功: {}", filePath);

        } catch (Exception e) {
            backup.setStatus("FAILED");
            backup.setErrorMessage(e.getMessage());
            backup.setCompletedTime(LocalDateTime.now());
            logger.error("数据备份失败", e);
        }

        BACKUP_STORE.put(backup.getId(), backup);
        return backup;
    }

    @Override
    @Transactional
    public ImBackup autoBackup(String backupType) {
        ImBackup backup = new ImBackup();
        backup.setId(backupIdCounter++);
        backup.setBackupType(backupType);
        backup.setStatus("IN_PROGRESS");
        backup.setProgress(0);
        backup.setOperator("SYSTEM");
        backup.setDescription("系统自动备份");
        backup.setCreateTime(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));

        try {
            // 生成备份文件名
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String typeSuffix = "AUTO_DAILY".equals(backupType) ? "daily" : "weekly";
            String fileName = "im_backup_" + typeSuffix + "_" + timestamp + ".sql";
            String filePath = backupPath + File.separator + fileName;

            // 确保备份目录存在
            Path backupDir = Paths.get(backupPath);
            if (!Files.exists(backupDir)) {
                Files.createDirectories(backupDir);
            }

            backup.setFileName(fileName);
            backup.setFilePath(filePath);
            backup.setProgress(20);

            // 执行数据库备份
            executeBackup(filePath, backup);

            // 获取文件大小
            File file = new File(filePath);
            backup.setFileSize(file.length());

            // 更新备份状态
            backup.setStatus("SUCCESS");
            backup.setProgress(100);
            backup.setCompletedTime(LocalDateTime.now());

            logger.info("自动备份成功: {}", filePath);

        } catch (Exception e) {
            backup.setStatus("FAILED");
            backup.setErrorMessage(e.getMessage());
            backup.setCompletedTime(LocalDateTime.now());
            logger.error("自动备份失败", e);
        }

        BACKUP_STORE.put(backup.getId(), backup);
        return backup;
    }

    /**
     * 执行数据库备份
     */
    private void executeBackup(String filePath, ImBackup backup) throws Exception {
        // 从 JDBC URL 提取数据库名称
        String dbName = extractDatabaseName(dbUrl);
        String host = extractHost(dbUrl);
        String port = extractPort(dbUrl);

        // 构建 mysqldump 命令
        ProcessBuilder processBuilder = new ProcessBuilder(
            MYSQLDUMP_CMD,
            "-h", host,
            "-P", port,
            "-u", dbUsername,
            "-p" + dbPassword,
            "--single-transaction",
            "--routines",
            "--triggers",
            "--events",
            dbName
        );

        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        // 写入备份文件
        try (InputStream inputStream = process.getInputStream();
             OutputStream outputStream = new FileOutputStream(filePath)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytes = 0;
            int progress = 20;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;

                // 更新进度（模拟）
                if (totalBytes % (1024 * 1024) == 0 && progress < 90) {
                    progress += 10;
                    backup.setProgress(progress);
                }
            }

            // 等待进程完成
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("mysqldump 命令执行失败，退出码: " + exitCode);
            }
        }
    }

    @Override
    public int deleteBackupById(Long id) {
        ImBackup backup = BACKUP_STORE.get(id);
        if (backup != null) {
            // 删除物理文件
            try {
                Files.deleteIfExists(Paths.get(backup.getFilePath()));
            } catch (IOException e) {
                logger.warn("删除备份文件失败: {}", backup.getFilePath(), e);
            }
            BACKUP_STORE.remove(id);
            return 1;
        }
        return 0;
    }

    @Override
    public int deleteBackupByIds(Long[] ids) {
        int count = 0;
        for (Long id : ids) {
            count += deleteBackupById(id);
        }
        return count;
    }

    @Override
    public String getBackupFilePath(Long backupId) {
        ImBackup backup = BACKUP_STORE.get(backupId);
        return backup != null ? backup.getFilePath() : null;
    }

    @Override
    @Transactional
    public boolean restoreBackup(Long backupId) {
        ImBackup backup = BACKUP_STORE.get(backupId);
        if (backup == null || !"SUCCESS".equals(backup.getStatus())) {
            logger.warn("备份记录不存在或状态无效: {}", backupId);
            return false;
        }

        String filePath = backup.getFilePath();
        if (!Files.exists(Paths.get(filePath))) {
            logger.warn("备份文件不存在: {}", filePath);
            return false;
        }

        try {
            // 从 JDBC URL 提取数据库连接信息
            String dbName = extractDatabaseName(dbUrl);
            String host = extractHost(dbUrl);
            String port = extractPort(dbUrl);

            // 构建 mysql 命令
            ProcessBuilder processBuilder = new ProcessBuilder(
                MYSQL_CMD,
                "-h", host,
                "-P", port,
                "-u", dbUsername,
                "-p" + dbPassword,
                dbName
            );

            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            // 写入 SQL 文件内容
            try (OutputStream outputStream = process.getOutputStream();
                 InputStream inputStream = new FileInputStream(filePath)) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            // 等待进程完成
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("mysql 命令执行失败，退出码: " + exitCode);
            }

            logger.info("数据恢复成功: {}", filePath);
            return true;

        } catch (Exception e) {
            logger.error("数据恢复失败: {}", filePath, e);
            return false;
        }
    }

    @Override
    public Map<String, Object> getBackupStatistics() {
        Map<String, Object> result = new HashMap<>();

        int totalCount = BACKUP_STORE.size();
        long totalSize = BACKUP_STORE.values().stream()
            .mapToLong(ImBackup::getFileSize)
            .sum();

        // 获取今天的日期（只比较日期部分，不比较时间）
        java.time.LocalDate today = LocalDateTime.now().toLocalDate();

        int todayCount = (int) BACKUP_STORE.values().stream()
            .filter(b -> {
                if (b.getCreateTime() == null) {
                    return false;
                }
                // 将 java.util.Date 转换为 LocalDate
                java.time.LocalDate createDate = b.getCreateTime()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
                return createDate.equals(today);
            })
            .count();

        int successCount = (int) BACKUP_STORE.values().stream()
            .filter(b -> "SUCCESS".equals(b.getStatus()))
            .count();

        result.put("totalCount", totalCount);
        result.put("totalSize", totalSize);
        result.put("todayCount", todayCount);
        result.put("successCount", successCount);
        result.put("failCount", totalCount - successCount);

        return result;
    }

    /**
     * 从 JDBC URL 提取数据库名称
     */
    private String extractDatabaseName(String url) {
        // jdbc:mysql://host:port/database
        int lastSlash = url.lastIndexOf("/");
        if (lastSlash > 0) {
            String dbPart = url.substring(lastSlash + 1);
            int questionMark = dbPart.indexOf("?");
            return questionMark > 0 ? dbPart.substring(0, questionMark) : dbPart;
        }
        return "im";
    }

    /**
     * 从 JDBC URL 提取主机地址
     */
    private String extractHost(String url) {
        // jdbc:mysql://host:port/database
        int hostStart = url.indexOf("://") + 3;
        int hostEnd = url.indexOf(":", hostStart);
        if (hostEnd > 0) {
            return url.substring(hostStart, hostEnd);
        }
        return "localhost";
    }

    /**
     * 从 JDBC URL 提取端口
     */
    private String extractPort(String url) {
        // jdbc:mysql://host:port/database
        int portStart = url.indexOf(":", url.indexOf("://") + 3);
        int portEnd = url.indexOf("/", portStart);
        if (portStart > 0 && portEnd > 0) {
            return url.substring(portStart + 1, portEnd);
        }
        return "3306";
    }
}
