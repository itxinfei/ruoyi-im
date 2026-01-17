package com.ruoyi.web.controller.im;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.web.domain.ImFileAsset;
import com.ruoyi.web.service.ImFileAssetService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IM文件资源管理控制器（管理后台）
 *
 * @author ruoyi
 * @date 2025-01-07
 */
@Controller
@RequestMapping("/im/file")
public class ImFileAssetController extends BaseController {

    private String prefix = "im/file";

    /**
     * 文件管理页面
     */
    @RequiresPermissions("im:file:list")
    @GetMapping()
    public String file() {
        return prefix + "/file";
    }

    @Autowired
    private ImFileAssetService imFileAssetService;

    @Autowired
    private RuoYiConfig ruoYiConfig;

    /**
     * 新增文件页面
     */
    @RequiresPermissions("im:file:add")
    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

    /**
     * 修改文件页面
     */
    @RequiresPermissions("im:file:edit")
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, org.springframework.ui.ModelMap mmap) {
        mmap.put("file", imFileAssetService.selectImFileAssetById(id));
        return prefix + "/edit";
    }

    /**
     * 查询文件资源列表
     */
    @RequiresPermissions("im:file:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ImFileAsset imFileAsset) {
        try {
            startPage();
            List<ImFileAsset> list = imFileAssetService.selectImFileAssetList(imFileAsset);
            return getDataTable(list);
        } catch (Exception e) {
            logger.error("查询文件列表失败", e);
            TableDataInfo tableDataInfo = new TableDataInfo();
            tableDataInfo.setCode(500);
            tableDataInfo.setMsg("查询失败: " + e.getMessage());
            tableDataInfo.setRows(new java.util.ArrayList<>());
            tableDataInfo.setTotal(0);
            return tableDataInfo;
        }
    }

    /**
     * 导出文件资源列表
     */
    @RequiresPermissions("im:file:export")
    @Log(title = "文件资源", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ImFileAsset imFileAsset) {
        List<ImFileAsset> list = imFileAssetService.selectImFileAssetList(imFileAsset);
        // 导出逻辑
    }

    /**
     * 获取文件资源详细信息
     */
    @RequiresPermissions("im:file:query")
    @GetMapping("/info/{id}")
    @ResponseBody
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(imFileAssetService.selectImFileAssetById(id));
    }

    /**
     * 新增文件资源
     */
    @RequiresPermissions("im:file:add")
    @Log(title = "文件资源", businessType = BusinessType.INSERT)
    @PostMapping
    @ResponseBody
    public AjaxResult add(ImFileAsset imFileAsset) {
        return toAjax(imFileAssetService.insertImFileAsset(imFileAsset));
    }

    /**
     * 修改文件资源（表单提交方式，用于编辑页面）
     */
    @RequiresPermissions("im:file:edit")
    @Log(title = "文件资源", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult edit(ImFileAsset imFileAsset) {
        return toAjax(imFileAssetService.updateImFileAsset(imFileAsset));
    }

    /**
     * 删除文件资源（POST方法，兼容RuoYi框架的删除操作）
     */
    @RequiresPermissions("im:file:remove")
    @Log(title = "文件资源", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(@RequestParam String ids) {
        return toAjax(imFileAssetService.deleteImFileAssetByIds(Convert.toLongArray(ids)));
    }

    /**
     * 文件统计
     */
    @RequiresPermissions("im:file:list")
    @GetMapping("/statistics")
    @ResponseBody
    public AjaxResult getStatistics() {
        return AjaxResult.success(imFileAssetService.getFileStatistics());
    }

    /**
     * 清理无效文件
     */
    @RequiresPermissions("im:file:remove")
    @Log(title = "清理无效文件", businessType = BusinessType.DELETE)
    @DeleteMapping("/clean")
    @ResponseBody
    public AjaxResult cleanInvalidFiles() {
        int count = imFileAssetService.cleanInvalidFiles();
        return AjaxResult.success("清理完成，共清理" + count + "个文件");
    }

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    @ResponseBody
    public AjaxResult uploadFile(@RequestParam("file") MultipartFile file,
                                 @RequestParam(required = false) Long userId) {
        try {
            if (file.isEmpty()) {
                return AjaxResult.error("请选择文件");
            }

            // 获取原始文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (StringUtils.isNotEmpty(originalFilename)) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            }

            // 上传文件
            String fileName = FileUploadUtils.upload(ruoYiConfig.getProfile(), file);

            // 创建文件记录
            ImFileAsset fileAsset = new ImFileAsset();
            fileAsset.setFileName(originalFilename);
            fileAsset.setFileType(getFileType(fileExtension));
            fileAsset.setFileSize(file.getSize());
            fileAsset.setFilePath(fileName);
            fileAsset.setUploaderId(userId != null ? userId : 1L);
            fileAsset.setMimeType(file.getContentType());

            imFileAssetService.insertImFileAsset(fileAsset);

            Map<String, Object> result = new HashMap<>();
            result.put("id", fileAsset.getId());
            result.put("fileName", originalFilename);
            result.put("filePath", fileName);
            result.put("fileSize", file.getSize());
            result.put("url", "/profile/" + fileName);

            return AjaxResult.success("上传成功", result);
        } catch (IOException e) {
            return AjaxResult.error("上传失败: " + e.getMessage());
        }
    }

    /**
     * 下载文件
     */
    @GetMapping("/download/{id}")
    @ResponseBody
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        try {
            ImFileAsset fileAsset = imFileAssetService.selectImFileAssetById(id);
            if (fileAsset == null) {
                return ResponseEntity.notFound().build();
            }

            // 构建文件路径
            String filePath = ruoYiConfig.getProfile() + fileAsset.getFilePath();
            Path path = Paths.get(filePath).normalize();
            Resource resource = new UrlResource(path.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            // 确定内容类型
            String contentType = fileAsset.getMimeType();
            if (StringUtils.isEmpty(contentType)) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileAsset.getFileName() + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 预览文件
     */
    @GetMapping("/preview/{id}")
    @ResponseBody
    public ResponseEntity<Resource> previewFile(@PathVariable Long id) {
        try {
            ImFileAsset fileAsset = imFileAssetService.selectImFileAssetById(id);
            if (fileAsset == null) {
                return ResponseEntity.notFound().build();
            }

            // 构建文件路径
            String filePath = ruoYiConfig.getProfile() + fileAsset.getFilePath();
            Path path = Paths.get(filePath).normalize();
            Resource resource = new UrlResource(path.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            // 确定内容类型
            String contentType = fileAsset.getMimeType();
            if (StringUtils.isEmpty(contentType)) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileAsset.getFileName() + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取文件访问URL
     */
    @GetMapping("/url/{id}")
    @ResponseBody
    public AjaxResult getFileUrl(@PathVariable Long id) {
        ImFileAsset fileAsset = imFileAssetService.selectImFileAssetById(id);
        if (fileAsset == null) {
            return AjaxResult.error("文件不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", fileAsset.getId());
        result.put("fileName", fileAsset.getFileName());
        result.put("filePath", fileAsset.getFilePath());
        result.put("fileSize", fileAsset.getFileSize());
        result.put("url", "/profile/" + fileAsset.getFilePath());
        result.put("downloadUrl", "/im/file/download/" + id);
        result.put("previewUrl", "/im/file/preview/" + id);

        return AjaxResult.success(result);
    }

    /**
     * 根据文件扩展名获取文件类型
     */
    private String getFileType(String extension) {
        extension = extension.toLowerCase();
        if (extension.matches("jpg|jpeg|png|gif|bmp|webp")) {
            return "image";
        } else if (extension.matches("mp4|avi|mov|wmv|flv|mkv")) {
            return "video";
        } else if (extension.matches("mp3|wav|flac|aac|ogg")) {
            return "audio";
        } else if (extension.matches("pdf|doc|docx|xls|xlsx|ppt|pptx|txt")) {
            return "document";
        } else if (extension.matches("zip|rar|7z|tar|gz")) {
            return "archive";
        } else {
            return "other";
        }
    }
}
