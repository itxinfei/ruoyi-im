package com.ruoyi.project.im.controller.im;

import com.ruoyi.framework.aspectj.lang.annotation.Log;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.framework.web.page.TableDataInfo;
import com.ruoyi.common.utils.text.Convert;
import com.ruoyi.framework.aspectj.lang.enums.BusinessType;
import com.ruoyi.common.utils.text.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.project.im.domain.ImFileAsset;
import com.ruoyi.project.im.service.ImFileAssetService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IM文件资源管理控制器（管理后台）
 *
 * @author ruoyi
 * @date 2025-01-17
 */
@Controller
@RequestMapping("/im/file")
public class ImFileAssetController extends BaseController {

    /** 页面前缀 */
    private static final String PAGE_PREFIX = "im/file";

    /**
     * 文件管理页面
     */
    @RequiresPermissions("im:file:list")
    @GetMapping()
    public String file() {
        return PAGE_PREFIX + "/file";
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
        return PAGE_PREFIX + "/add";
    }

    /**
     * 修改文件页面
     */
    @RequiresPermissions("im:file:edit")
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, org.springframework.ui.ModelMap mmap) {
        mmap.put("file", imFileAssetService.selectImFileAssetById(id));
        return PAGE_PREFIX + "/edit";
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
        ExcelUtil<ImFileAsset> util = new ExcelUtil<>(ImFileAsset.class);
        util.exportExcel(response, list, "文件资源数据");
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
     * 新增文件资产
     */
    @RequiresPermissions("im:file:add")
    @Log(title = "文件资产", businessType = BusinessType.INSERT)
    @PostMapping("/add")
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
     * 删除文件资源（支持单个和批量删除）
     */
    @RequiresPermissions("im:file:remove")
    @Log(title = "文件资源", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(@RequestParam String ids) {
        try {
            Long[] idArray = Convert.toLongArray(ids);
            int count = imFileAssetService.deleteImFileAssetByIds(idArray);
            return toAjax(count);
        } catch (Exception e) {
            logger.error("删除文件失败", e);
            return AjaxResult.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件资源（路径参数方式，兼容REST风格）
     */
    @RequiresPermissions("im:file:remove")
    @Log(title = "文件资源", businessType = BusinessType.DELETE)
    @PostMapping("/remove/{ids}")
    @ResponseBody
    public AjaxResult removeByPath(@PathVariable String ids) {
        return remove(ids);
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
    @Log(title = "文件上传", businessType = BusinessType.INSERT)
    public AjaxResult uploadFile(@RequestParam("file") MultipartFile file,
                                 @RequestParam(required = false) Long userId) {
        try {
            if (file.isEmpty()) {
                return AjaxResult.error("请选择文件");
            }

            // 文件大小限制 (100MB)
            long maxSize = 100 * 1024 * 1024;
            if (file.getSize() > maxSize) {
                return AjaxResult.error("文件大小不能超过100MB");
            }

            // 获取文件名和扩展名
            String originalFilename = file.getOriginalFilename();
            if (StringUtils.isEmpty(originalFilename)) {
                return AjaxResult.error("文件名不能为空");
            }

            // 验证文件扩展名
            String extension = getFileExtension(originalFilename).toLowerCase();
            if (!isAllowedFileType(extension)) {
                return AjaxResult.error("不支持的文件类型：" + extension);
            }

            // 验证文件MIME类型
            String contentType = file.getContentType();
            if (!isAllowedMimeType(contentType, extension)) {
                return AjaxResult.error("文件类型与扩展名不匹配");
            }

            // 调用Service层上传文件
            ImFileAsset fileAsset = imFileAssetService.uploadFile(file, userId);

            Map<String, Object> result = new HashMap<>();
            result.put("id", fileAsset.getId());
            result.put("fileName", fileAsset.getFileName());
            result.put("filePath", fileAsset.getFilePath());
            result.put("fileSize", fileAsset.getFileSize());
            result.put("fileType", fileAsset.getFileType());
            result.put("Path", fileAsset.getFilePath());

            return AjaxResult.success("上传成功", result);
        } catch (IllegalArgumentException e) {
            logger.warn("文件上传参数校验失败：{}", e.getMessage());
            return AjaxResult.error(e.getMessage());
        } catch (Exception e) {
            logger.error("文件上传失败", e);
            return AjaxResult.error("上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (StringUtils.isEmpty(filename)) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1);
        }
        return "";
    }

    /**
     * 验证文件类型是否允许
     */
    private boolean isAllowedFileType(String extension) {
        // 允许的文件类型白名单
        String[] allowedTypes = {
            // 图片
            "jpg", "jpeg", "png", "gif", "bmp", "webp",
            // 文档
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt",
            // 压缩文件
            "zip", "rar", "7z", "tar", "gz",
            // 音频
            "mp3", "wav", "ogg", "m4a",
            // 视频
            "mp4", "avi", "mov", "wmv", "flv", "mkv"
        };

        for (String allowedType : allowedTypes) {
            if (allowedType.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 验证MIME类型是否与扩展名匹配
     */
    private boolean isAllowedMimeType(String mimeType, String extension) {
        if (StringUtils.isEmpty(mimeType)) {
            return false;
        }

        // 扩展名到MIME类型的映射
        Map<String, String> extensionToMime = new HashMap<>();
        extensionToMime.put("jpg", "image/jpeg");
        extensionToMime.put("jpeg", "image/jpeg");
        extensionToMime.put("png", "image/png");
        extensionToMime.put("gif", "image/gif");
        extensionToMime.put("bmp", "image/bmp");
        extensionToMime.put("webp", "image/webp");
        extensionToMime.put("pdf", "application/pdf");
        extensionToMime.put("doc", "application/msword");
        extensionToMime.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        extensionToMime.put("xls", "application/vnd.ms-excel");
        extensionToMime.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        extensionToMime.put("ppt", "application/vnd.ms-powerpoint");
        extensionToMime.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        extensionToMime.put("txt", "text/plain");
        extensionToMime.put("zip", "application/zip");
        extensionToMime.put("rar", "application/x-rar-compressed");
        extensionToMime.put("7z", "application/x-7z-compressed");
        extensionToMime.put("mp3", "audio/mpeg");
        extensionToMime.put("wav", "audio/wav");
        extensionToMime.put("mp4", "video/mp4");
        extensionToMime.put("avi", "video/x-msvideo");

        String expectedMime = extensionToMime.get(extension.toLowerCase());
        if (expectedMime == null) {
            // 如果没有映射，允许通过（因为有些MIME类型可能不在列表中）
            return true;
        }

        // 检查MIME类型是否匹配（允许前缀匹配）
        return mimeType.startsWith(expectedMime) || expectedMime.startsWith(mimeType);
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
                logger.warn("下载文件失败：文件不存在，ID：{}", id);
                return ResponseEntity.notFound().build();
            }

            // 检查文件状态
            if (!fileAsset.isActive()) {
                logger.warn("下载文件失败：文件已删除，ID：{}", id);
                return ResponseEntity.notFound().build();
            }

            // 构建文件路径
            String filePath = fileAsset.getFilePath();
            // 去掉路径中的/profile前缀
            if (filePath.startsWith("/profile/")) {
                filePath = filePath.substring("/profile/".length());
            }
            String fullPath = ruoYiConfig.getProfile() + "/" + filePath;
            Path path = Paths.get(fullPath).normalize();
            Resource resource = new UrlResource(path.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                logger.warn("下载文件失败：文件无法读取，路径：{}", fullPath);
                return ResponseEntity.notFound().build();
            }

            // 增加下载次数
            imFileAssetService.incrementDownloadCount(id);

            // 确定内容类型
            String contentType = fileAsset.getFileType();
            if (StringUtils.isEmpty(contentType)) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileAsset.getFileName() + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            logger.error("下载文件异常，文件ID：{}", id, e);
            return ResponseEntity.internalServerError().build();
        } catch (Exception e) {
            logger.error("下载文件异常", e);
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
                logger.warn("预览文件失败：文件不存在，ID：{}", id);
                return ResponseEntity.notFound().build();
            }

            // 检查文件状态
            if (!fileAsset.isActive()) {
                logger.warn("预览文件失败：文件已删除，ID：{}", id);
                return ResponseEntity.notFound().build();
            }

            // 构建文件路径
            String filePath = fileAsset.getFilePath();
            // 去掉路径中的/profile前缀
            if (filePath.startsWith("/profile/")) {
                filePath = filePath.substring("/profile/".length());
            }
            String fullPath = ruoYiConfig.getProfile() + "/" + filePath;
            Path path = Paths.get(fullPath).normalize();
            Resource resource = new UrlResource(path.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                logger.warn("预览文件失败：文件无法读取，路径：{}", fullPath);
                return ResponseEntity.notFound().build();
            }

            // 确定内容类型
            String contentType = fileAsset.getFileType();
            if (StringUtils.isEmpty(contentType)) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileAsset.getFileName() + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            logger.error("预览文件异常，文件ID：{}", id, e);
            return ResponseEntity.internalServerError().build();
        } catch (Exception e) {
            logger.error("预览文件异常", e);
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
}
