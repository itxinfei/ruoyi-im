package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImFile;
import com.ruoyi.im.service.ImFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * File Controller
 */
@RestController
@RequestMapping("/api/im/files")
public class ImFileController {

    @Autowired
    private ImFileService imFileService;

    /**
     * Get file list
     */
    @GetMapping
    public Result<List<ImFile>> getFiles() {
        return Result.success(imFileService.list());
    }

    /**
     * Upload file
     */
    @PostMapping("/upload")
    public Result<ImFile> uploadFile(@RequestParam("file") MultipartFile file) {
        ImFile imFile = new ImFile();
        imFile.setFileName(file.getOriginalFilename());
        imFile.setFileSize(file.getSize());
        imFileService.save(imFile);
        return Result.success(imFile);
    }

    /**
     * Delete file
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteFile(@PathVariable Long id) {
        return Result.success(imFileService.removeById(id));
    }

    /**
     * Get file by id
     */
    @GetMapping("/{id}")
    public Result<ImFile> getFile(@PathVariable Long id) {
        return Result.success(imFileService.getById(id));
    }
}
