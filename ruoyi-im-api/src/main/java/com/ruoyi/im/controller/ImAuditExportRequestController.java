package com.ruoyi.im.controller;

import com.ruoyi.im.common.PageResult;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImAuditExportRequest;
import com.ruoyi.im.service.ImAuditExportRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 审计导出请求控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/api/im/auditExport")
public class ImAuditExportRequestController extends BaseController {

    @Autowired
    private ImAuditExportRequestService imAuditExportRequestService;

    /**
     * 查询审计导出请求列表
     */
    @GetMapping("/list")
    public Result<PageResult<ImAuditExportRequest>> list(@RequestParam(required = false) Long userId,
                                   @RequestParam(required = false) String status) {
        startPage();
        ImAuditExportRequest query = new ImAuditExportRequest();
        query.setUserId(userId);
        query.setStatus(status);
        List<ImAuditExportRequest> list = imAuditExportRequestService.selectImAuditExportRequestList(query);
        return getDataTable(list);
    }

    /**
     * 查询审计导出请求详细
     */
    @GetMapping("/{id}")
    public Result<ImAuditExportRequest> getInfo(@PathVariable Long id) {
        ImAuditExportRequest exportRequest = imAuditExportRequestService.selectImAuditExportRequestById(id);
        if (exportRequest != null) {
            return Result.success(exportRequest);
        } else {
            return Result.error(404, "审计导出请求不存在");
        }
    }

    /**
     * 根据用户ID查询审计导出请求列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<ImAuditExportRequest>> listByUserId(@PathVariable Long userId) {
        List<ImAuditExportRequest> list = imAuditExportRequestService.selectImAuditExportRequestByUserId(userId);
        return Result.success(list);
    }

    /**
     * 根据状态查询审计导出请求列表
     */
    @GetMapping("/status/{status}")
    public Result<List<ImAuditExportRequest>> listByStatus(@PathVariable String status) {
        List<ImAuditExportRequest> list = imAuditExportRequestService.selectImAuditExportRequestByStatus(status);
        return Result.success(list);
    }

    /**
     * 新增审计导出请求
     */
    @PostMapping
    public Result<ImAuditExportRequest> add(@RequestBody ImAuditExportRequest exportRequest) {
        int rows = imAuditExportRequestService.insertImAuditExportRequest(exportRequest);
        if (rows > 0) {
            return Result.success(exportRequest);
        } else {
            return Result.error(500, "审计导出请求添加失败");
        }
    }

    /**
     * 创建审计导出请求
     */
    @PostMapping("/create")
    public Result<Void> createExportRequest(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        String startTime = params.get("startTime").toString();
        String endTime = params.get("endTime").toString();
        String operationType = params.get("operationType") != null ? params.get("operationType").toString() : null;
        String targetType = params.get("targetType") != null ? params.get("targetType").toString() : null;
        Long targetId = params.get("targetId") != null ? Long.valueOf(params.get("targetId").toString()) : null;
        String format = params.get("format") != null ? params.get("format").toString() : "CSV";
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime start = LocalDateTime.parse(startTime, formatter);
        LocalDateTime end = LocalDateTime.parse(endTime, formatter);
        
        int rows = imAuditExportRequestService.createExportRequest(userId, start, end, operationType, targetType, targetId, format);
        
        if (rows > 0) {
            return Result.success();
        } else {
            return Result.error(500, "审计导出请求创建失败");
        }
    }

    /**
     * 更新导出请求状态
     */
    @PostMapping("/updateStatus")
    public Result<Void> updateStatus(@RequestBody Map<String, Object> params) {
        Long requestId = Long.valueOf(params.get("requestId").toString());
        String status = params.get("status").toString();
        String fileUrl = params.get("fileUrl") != null ? params.get("fileUrl").toString() : null;
        String errorMessage = params.get("errorMessage") != null ? params.get("errorMessage").toString() : null;
        
        int rows = imAuditExportRequestService.updateExportStatus(requestId, status, fileUrl, errorMessage);
        
        if (rows > 0) {
            return Result.success();
        } else {
            return Result.error(500, "导出请求状态更新失败");
        }
    }

    /**
     * 修改审计导出请求
     */
    @PutMapping
    public Result<Void> edit(@RequestBody ImAuditExportRequest exportRequest) {
        int rows = imAuditExportRequestService.updateImAuditExportRequest(exportRequest);
        if (rows > 0) {
            return Result.success();
        } else {
            return Result.error(500, "审计导出请求修改失败");
        }
    }
}
