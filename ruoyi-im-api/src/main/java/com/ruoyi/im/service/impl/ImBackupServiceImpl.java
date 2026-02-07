package com.ruoyi.im.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.im.domain.ImBackup;
import com.ruoyi.im.domain.ImConversationMember;
import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.mapper.ImBackupMapper;
import com.ruoyi.im.mapper.ImConversationMemberMapper;
import com.ruoyi.im.mapper.ImMessageMapper;
import com.ruoyi.im.service.ImUserService;
import com.ruoyi.im.service.ImBackupService;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.util.ExceptionHandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 数据备份服务实现
 * 使用数据库表存储备份记录，文件系统存储备份文件
 *
 * @author ruoyi
 */
@Service
public class ImBackupServiceImpl implements ImBackupService {

    private static final Logger logger = LoggerFactory.getLogger(ImBackupServiceImpl.class);

    private final ImBackupMapper imBackupMapper;
    private final ImUserService imUserService;
    private final ImMessageMapper imMessageMapper;
    private final ImConversationMemberMapper imConversationMemberMapper;
    private final ObjectMapper objectMapper;
    private final String backupPath;
    private final String datasourceUrl;
    private final String datasourceUsername;
    private final String datasourcePassword;

    public ImBackupServiceImpl(ImBackupMapper imBackupMapper,
                              ImUserService imUserService,
                              ImMessageMapper imMessageMapper,
                              ImConversationMemberMapper imConversationMemberMapper,
                              ObjectMapper objectMapper,
                              @Value("${backup.path:/tmp/im-backup}") String backupPath,
                              @Value("${spring.datasource.url:jdbc:mysql://localhost:3306/ruoyi-im}") String datasourceUrl,
                              @Value("${spring.datasource.username:root}") String datasourceUsername,
                              @Value("${spring.datasource.password:}") String datasourcePassword) {
        this.imBackupMapper = imBackupMapper;
        this.imUserService = imUserService;
        this.imMessageMapper = imMessageMapper;
        this.imConversationMemberMapper = imConversationMemberMapper;
        this.objectMapper = objectMapper;
        this.backupPath = backupPath;
        this.datasourceUrl = datasourceUrl;
        this.datasourceUsername = datasourceUsername;
        this.datasourcePassword = datasourcePassword;
    }

    @Override
    public List<Map<String, Object>> getBackupList() {
        List<ImBackup> backups = imBackupMapper.selectAllOrderByCreateTime();
        List<Map<String, Object>> result = new ArrayList<>();

        for (ImBackup backup : backups) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", backup.getId());
            map.put("fileName", backup.getFileName());
            map.put("description", backup.getDescription());
            map.put("fileSize", backup.getFileSize());
            map.put("backupType", backup.getBackupType());
            map.put("status", backup.getStatus());
            map.put("createTime", backup.getCreateTime());
            map.put("completeTime", backup.getCompleteTime());
            map.put("creatorName", backup.getCreatorName());
            result.add(map);
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createBackup(String description) {
        logger.info("开始创建数据备份，描述: {}", description);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = "im_backup_" + timestamp + ".sql";

        // 创建备份记录
        ImBackup backup = new ImBackup();
        backup.setFileName(fileName);
        backup.setDescription(description != null && !description.isEmpty() ? description : "手动备份");
        backup.setBackupType("full");
        backup.setStatus("in_progress");
        backup.setCreateTime(LocalDateTime.now());
        backup.setCreatorId(1L);
        backup.setCreatorName("系统管理员");

        imBackupMapper.insert(backup);

        // 异步执行备份
        performBackup(backup);

        Map<String, Object> result = new HashMap<>();
        result.put("id", backup.getId());
        result.put("fileName", fileName);
        result.put("description", backup.getDescription());
        result.put("status", backup.getStatus());
        result.put("createTime", backup.getCreateTime());

        logger.info("备份任务已创建，ID: {}, 文件名: {}", backup.getId(), fileName);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreBackup(Long backupId) {
        logger.info("开始恢复备份，ID: {}", backupId);

        ImBackup backup = imBackupMapper.selectById(backupId);
        if (backup == null) {
            ExceptionHandlerUtil.throwBusinessException("备份文件不存在");
        }

        if (!"completed".equals(backup.getStatus())) {
            ExceptionHandlerUtil.throwBusinessException("备份文件状态异常，无法恢复");
        }

        if (backup.getFilePath() == null || backup.getFilePath().isEmpty()) {
            ExceptionHandlerUtil.throwBusinessException("备份文件路径为空");
        }

        // 执行恢复
        performRestore(backup);

        logger.info("备份恢复完成，ID: {}", backupId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBackup(Long backupId) {
        logger.info("删除备份，ID: {}", backupId);

        ImBackup backup = imBackupMapper.selectById(backupId);
        if (backup == null) {
            ExceptionHandlerUtil.throwBusinessException("备份文件不存在");
        }

        // 删除物理文件
        if (backup.getFilePath() != null && !backup.getFilePath().isEmpty()) {
            try {
                Files.deleteIfExists(Paths.get(backup.getFilePath()));
                logger.info("已删除备份文件: {}", backup.getFilePath());
            } catch (IOException e) {
                ExceptionHandlerUtil.logWarning(logger, "删除备份文件失败: {}", e, backup.getFilePath());
            }
        }

        // 删除数据库记录
        imBackupMapper.deleteById(backupId);

        logger.info("备份删除完成，ID: {}", backupId);
    }

    @Override
    public Map<String, Object> getBackupDetail(Long backupId) {
        ImBackup backup = imBackupMapper.selectById(backupId);
        if (backup == null) {
            ExceptionHandlerUtil.throwBusinessException("备份文件不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", backup.getId());
        result.put("fileName", backup.getFileName());
        result.put("description", backup.getDescription());
        result.put("filePath", backup.getFilePath());
        result.put("fileSize", backup.getFileSize());
        result.put("backupType", backup.getBackupType());
        result.put("status", backup.getStatus());
        result.put("creatorId", backup.getCreatorId());
        result.put("creatorName", backup.getCreatorName());
        result.put("errorMessage", backup.getErrorMessage());
        result.put("createTime", backup.getCreateTime());
        result.put("completeTime", backup.getCompleteTime());

        return result;
    }

    @Override
    public Map<String, Object> exportUserData(Long userId) {
        logger.info("导出用户数据，userId: {}", userId);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = "user_data_" + userId + "_" + timestamp + ".json";

        try {
            // 查询用户数据
            ImUser user = imUserService.getUserEntityById(userId);
            if (user == null) {
                ExceptionHandlerUtil.throwBusinessException("用户不存在");
            }

            // 构建导出数据
            Map<String, Object> userData = new HashMap<>();
            userData.put("user", user);
            userData.put("exportTime", LocalDateTime.now().toString());

            // 查询用户的消息
            ImMessage messageQuery = new ImMessage();
            messageQuery.setSenderId(userId);
            List<ImMessage> messages = imMessageMapper.selectImMessageList(messageQuery);
            userData.put("messageCount", messages.size());

            // 查询用户的会话
            List<ImConversationMember> members = imConversationMemberMapper.selectByUserId(userId);
            userData.put("conversationCount", members.size());

            // 保存到文件
            String filePath = backupPath + "/exports/" + fileName;
            Path exportPath = Paths.get(filePath);
            Files.createDirectories(exportPath.getParent());

            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(userData);
            Files.write(exportPath, json.getBytes());

            Map<String, Object> result = new HashMap<>();
            result.put("userId", userId);
            result.put("userName", user.getNickname());
            result.put("fileName", fileName);
            result.put("filePath", filePath);
            result.put("exportTime", LocalDateTime.now());
            result.put("fileSize", Files.size(exportPath));

            logger.info("用户数据导出完成，userId: {}, fileName: {}", userId, fileName);
            return result;

        } catch (Exception e) {
            ExceptionHandlerUtil.throwBusinessException(logger, "导出用户数据失败: userId=" + userId, e);
            return null; // 这行不会执行，只是为了满足编译器要求
        }
    }

    @Override
    public Map<String, Object> getBackupStatistics() {
        Map<String, Object> stats = new HashMap<>();

        List<ImBackup> backups = imBackupMapper.selectAllOrderByCreateTime();

        stats.put("totalBackups", backups.size());
        stats.put("totalSize", backups.stream().mapToLong(b -> b.getFileSize() != null ? b.getFileSize() : 0L).sum());

        long completedCount = backups.stream().filter(b -> "completed".equals(b.getStatus())).count();
        stats.put("completedCount", completedCount);

        if (!backups.isEmpty()) {
            Optional<ImBackup> latestBackup = backups.stream()
                    .filter(b -> "completed".equals(b.getStatus()))
                    .max(Comparator.comparing(ImBackup::getCreateTime));
            latestBackup.ifPresent(backup -> {
                stats.put("lastBackupTime", backup.getCreateTime());
                stats.put("lastBackupId", backup.getId());
                stats.put("lastBackupFile", backup.getFileName());
            });
        }

        return stats;
    }

    /**
     * 执行数据库备份
     */
    private void performBackup(ImBackup backup) {
        new Thread(() -> {
            try {
                logger.info("开始执行备份任务，ID: {}", backup.getId());

                // 确保备份目录存在
                Path backupDir = Paths.get(backupPath);
                Files.createDirectories(backupDir);

                String filePath = backupDir.resolve(backup.getFileName()).toString();

                // 构建mysqldump命令
                List<String> command = buildMysqldumpCommand();

                ProcessBuilder processBuilder = new ProcessBuilder(command);
                processBuilder.redirectErrorStream(true);

                Process process = processBuilder.start();

                // 写入备份文件
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                     BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath)))) {

                    String line;
                    while ((line = reader.readLine()) != null) {
                        writer.write(line);
                        writer.newLine();
                    }
                }

                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    // 备份成功，更新记录
                    File backupFile = new File(filePath);
                    imBackupMapper.updateCompleteInfo(backup.getId(), filePath, backupFile.length());
                    logger.info("备份执行成功，ID: {}, 文件大小: {} bytes", backup.getId(), backupFile.length());
                } else {
                    // 备份失败
                    imBackupMapper.updateStatus(backup.getId(), "failed", "mysqldump退出码: " + exitCode);
                    logger.error("备份执行失败，退出码: {}", exitCode);
                }

            } catch (Exception e) {
                ExceptionHandlerUtil.logError(logger, "备份执行异常: backupId={}", e, backup.getId());
                imBackupMapper.updateStatus(backup.getId(), "failed", e.getMessage());
            }
        }).start();
    }

    /**
     * 执行数据库恢复
     */
    private void performRestore(ImBackup backup) {
        try {
            String filePath = backup.getFilePath();
            File backupFile = new File(filePath);

            if (!backupFile.exists()) {
                ExceptionHandlerUtil.throwBusinessException("备份文件不存在: " + filePath);
            }

            // 构建mysql命令
            List<String> command = buildMysqlCommand();
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            // 写入SQL文件
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                 BufferedReader reader = new BufferedReader(new java.io.FileReader(backupFile))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    writer.write(line);
                    writer.newLine();
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                ExceptionHandlerUtil.throwBusinessException("恢复失败，mysql退出码: " + exitCode);
            }

            logger.info("数据库恢复成功，备份ID: {}", backup.getId());

        } catch (Exception e) {
            ExceptionHandlerUtil.throwBusinessException(logger, "数据库恢复失败: backupId=" + backup.getId(), e);
        }
    }

    /**
     * 构建mysqldump命令
     */
    private List<String> buildMysqldumpCommand() {
        List<String> command = new ArrayList<>();
        command.add("mysqldump");
        command.add("--single-transaction");
        command.add("--routines");
        command.add("--triggers");
        command.add("--events");

        // 从JDBC URL中提取数据库连接信息
        String dbName = extractDatabaseName(datasourceUrl);
        String host = extractHost(datasourceUrl);
        String port = extractPort(datasourceUrl);

        command.add("--host=" + host);
        command.add("--port=" + port);
        command.add("--user=" + datasourceUsername);

        if (datasourcePassword != null && !datasourcePassword.isEmpty()) {
            command.add("--password=" + datasourcePassword);
        }

        command.add(dbName);

        return command;
    }

    /**
     * 构建mysql命令
     */
    private List<String> buildMysqlCommand() {
        List<String> command = new ArrayList<>();
        command.add("mysql");

        String host = extractHost(datasourceUrl);
        String port = extractPort(datasourceUrl);
        String dbName = extractDatabaseName(datasourceUrl);

        command.add("--host=" + host);
        command.add("--port=" + port);
        command.add("--user=" + datasourceUsername);

        if (datasourcePassword != null && !datasourcePassword.isEmpty()) {
            command.add("--password=" + datasourcePassword);
        }

        command.add(dbName);

        return command;
    }

    /**
     * 从JDBC URL中提取数据库名
     */
    private String extractDatabaseName(String url) {
        // 格式: jdbc:mysql://host:port/database
        String[] parts = url.split("/");
        if (parts.length > 0) {
            String lastPart = parts[parts.length - 1];
            // 移除参数
            int paramIndex = lastPart.indexOf("?");
            if (paramIndex > 0) {
                lastPart = lastPart.substring(0, paramIndex);
            }
            return lastPart;
        }
        return "ruoyi-im";
    }

    /**
     * 从JDBC URL中提取主机
     */
    private String extractHost(String url) {
        // 格式: jdbc:mysql://host:port/database
        String[] parts = url.split("//");
        if (parts.length > 1) {
            String hostPort = parts[1].split("/")[0];
            String[] hostParts = hostPort.split(":");
            return hostParts[0];
        }
        return "localhost";
    }

    /**
     * 从JDBC URL中提取端口
     */
    private String extractPort(String url) {
        // 格式: jdbc:mysql://host:port/database
        String[] parts = url.split("//");
        if (parts.length > 1) {
            String hostPort = parts[1].split("/")[0];
            String[] hostParts = hostPort.split(":");
            if (hostParts.length > 1) {
                return hostParts[1];
            }
        }
        return "3306";
    }
}
