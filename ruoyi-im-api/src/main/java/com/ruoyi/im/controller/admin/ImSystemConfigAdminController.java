package com.ruoyi.im.controller.admin;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.config.FileUploadConfig;
import com.ruoyi.im.service.ImSystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * 管理员-系统配置控制器
 *
 * @author ruoyi
 */
@Tag(name = "管理员-系统配置", description = "管理员管理系统全局参数接口")
@RestController
@RequestMapping("/api/admin/config")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN', 'ADMIN')")
public class ImSystemConfigAdminController {

    private final ImSystemConfigService configService;
    private final FileUploadConfig fileUploadConfig;

    public ImSystemConfigAdminController(ImSystemConfigService configService,
                                          FileUploadConfig fileUploadConfig) {
        this.configService = configService;
        this.fileUploadConfig = fileUploadConfig;
    }

    @Operation(summary = "获取所有系统配置")
    @GetMapping("/all")
    public Result<Map<String, Object>> getAllConfigs() {
        return Result.success(configService.getAllSystemConfigs());
    }

    @Operation(summary = "更新系统配置", description = "批量更新系统配置，传入键值对Map")
    @PutMapping("/update")
    public Result<Void> updateConfig(@RequestBody Map<String, String> configs) {
        if (configs != null && !configs.isEmpty()) {
            for (Map.Entry<String, String> entry : configs.entrySet()) {
                configService.updateSystemConfig(entry.getKey(), entry.getValue());
            }
        }
        return Result.success("更新成功");
    }

    @Operation(summary = "设置消息撤回时限")
    @PutMapping("/recall-limit")
    public Result<Void> setRecallLimit(@RequestParam Integer minutes) {
        configService.setMessageRecallTimeLimit(minutes);
        return Result.success("设置成功");
    }

    @Operation(summary = "上传系统Logo")
    @PostMapping("/logo")
    public Result<String> uploadLogo(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.error("请选择文件");
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                return Result.error("文件名不能为空");
            }
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            if (!extension.toLowerCase().matches("\\.(png|jpg|jpeg|gif|svg|webp)")) {
                return Result.error("只支持图片格式：png、jpg、jpeg、gif、svg、webp");
            }

            long maxSize = 2 * 1024 * 1024;
            if (file.getSize() > maxSize) {
                return Result.error("文件大小不能超过2MB");
            }

            String logoDir = fileUploadConfig.getAbsoluteUploadPath() + "/logo";
            Path logoPath = Paths.get(logoDir);
            if (!Files.exists(logoPath)) {
                Files.createDirectories(logoPath);
            }

            String fileName = "logo" + extension;
            Path filePath = logoPath.resolve(fileName);
            Files.write(filePath, file.getBytes());

            String logoUrl = "/uploads/logo/" + fileName;
            configService.updateSystemConfig("system.logo.url", logoUrl);

            return Result.success(logoUrl, "上传成功");
        } catch (IOException e) {
            return Result.error("上传失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取系统Logo")
    @GetMapping("/logo")
    public Result<String> getLogo() {
        String logoUrl = configService.getConfigValue("system.logo.url");
        return Result.success(logoUrl);
    }

    @Operation(summary = "获取敏感词列表")
    @GetMapping("/sensitive-words")
    public Result<Map<String, Object>> getSensitiveWords() {
        Map<String, Object> result = new java.util.HashMap<>();
        String wordsStr = configService.getConfigValue("sensitive.words");
        String strategy = configService.getConfigValue("sensitive.strategy");
        
        result.put("words", wordsStr != null ? wordsStr.split(",") : new String[0]);
        result.put("strategy", strategy != null ? strategy : "replace");
        
        return Result.success(result);
    }

    @Operation(summary = "保存敏感词列表")
    @PostMapping("/sensitive-words")
    public Result<Void> saveSensitiveWords(@RequestBody Map<String, Object> data) {
        String strategy = (String) data.get("strategy");
        @SuppressWarnings("unchecked")
        java.util.List<String> words = (java.util.List<String>) data.get("words");
        
        configService.updateSystemConfig("sensitive.strategy", strategy);
        configService.updateSystemConfig("sensitive.words", String.join(",", words));
        
        return Result.success("保存成功");
    }
}