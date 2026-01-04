package com.ruoyi.im.controller;

import com.ruoyi.im.domain.ImAuditExportRequest;
import com.ruoyi.im.service.ImAuditExportRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 审计导出请求控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/api/im/auditExport")
public class ImAuditExportRequestController {

    @Autowired
    private ImAuditExportRequestService imAuditExportRequestService;

    /**
     * 查询审计导出请求列表
     */
    @GetMapping("/list")
    public Map<String, Object> list(@RequestParam(required = false) Long userId,
                                   @RequestParam(required = false) String status,
                                   @RequestParam(defaultValue = "1") Integer pageNum,
                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        
        ImAuditExportRequest query = new ImAuditExportRequest();
        query.setUserId(userId);
        query.setStatus(status);
        
        List<ImAuditExportRequest> allRequests = imAuditExportRequestService.selectImAuditExportRequestList(query);
        
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, allRequests.size());
        
        List<ImAuditExportRequest> pagedRequests = start < allRequests.size() ? 
            allRequests.subList(start, end) : java.util.Collections.emptyList();
        
        Map<String, Object> pageResult = new HashMap<>();
        pageResult.put("rows", pagedRequests);
        pageResult.put("total", allRequests.size());
        pageResult.put("pageNum", pageNum);
        pageResult.put("pageSize", pageSize);
        
        result.put("code", 200);
        result.put("msg", "查询成功");
        result.put("data", pageResult);
        
        return result;
    }

    /**
     * 查询审计导出请求详细
     */
    @GetMapping("/{id}")
    public Map<String, Object> getInfo(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        ImAuditExportRequest exportRequest = imAuditExportRequestService.selectImAuditExportRequestById(id);
        
        if (exportRequest != null) {
            result.put("code", 200);
            result.put("msg", "查询成功");
            result.put("data", exportRequest);
        } else {
            result.put("code", 404);
            result.put("msg", "审计导出请求不存在");
        }
        
        return result;
    }

    /**
     * 根据用户ID查询审计导出请求列表
     */
    @GetMapping("/user/{userId}")
    public Map<String, Object> listByUserId(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        List<ImAuditExportRequest> list = imAuditExportRequestService.selectImAuditExportRequestByUserId(userId);
        
        result.put("code", 200);
        result.put("msg", "查询成功");
        result.put("data", list);
        
        return result;
    }

    /**
     * 根据状态查询审计导出请求列表
     */
    @GetMapping("/status/{status}")
    public Map<String, Object> listByStatus(@PathVariable String status) {
        Map<String, Object> result = new HashMap<>();
        
        List<ImAuditExportRequest> list = imAuditExportRequestService.selectImAuditExportRequestByStatus(status);
        
        result.put("code", 200);
        result.put("msg", "查询成功");
        result.put("data", list);
        
        return result;
    }

    /**
     * 新增审计导出请求
     */
    @PostMapping
    public Map<String, Object> add(@RequestBody ImAuditExportRequest exportRequest) {
        Map<String, Object> result = new HashMap<>();
        
        int rows = imAuditExportRequestService.insertImAuditExportRequest(exportRequest);
        
        if (rows > 0) {
            result.put("code", 200);
            result.put("msg", "审计导出请求添加成功");
            result.put("data", exportRequest);
        } else {
            result.put("code", 500);
            result.put("msg", "审计导出请求添加失败");
        }
        
        return result;
    }

    /**
     * 创建审计导出请求
     */
    @PostMapping("/create")
    public Map<String, Object> createExportRequest(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        
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
            result.put("code", 200);
            result.put("msg", "审计导出请求创建成功");
        } else {
            result.put("code", 500);
            result.put("msg", "审计导出请求创建失败");
        }
        
        return result;
    }

    /**
     * 更新导出请求状态
     */
    @PostMapping("/updateStatus")
    public Map<String, Object> updateStatus(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        
        Long requestId = Long.valueOf(params.get("requestId").toString());
        String status = params.get("status").toString();
        String fileUrl = params.get("fileUrl") != null ? params.get("fileUrl").toString() : null;
        String errorMessage = params.get("errorMessage") != null ? params.get("errorMessage").toString() : null;
        
        int rows = imAuditExportRequestService.updateExportStatus(requestId, status, fileUrl, errorMessage);
        
        if (rows > 0) {
            result.put("code", 200);
            result.put("msg", "导出请求状态更新成功");
        } else {
            result.put("code", 500);
            result.put("msg", "导出请求状态更新失败");
        }
        
        return result;
    }

    /**
     * 修改审计导出请求
     */
    @PutMapping
    public Map<String, Object> edit(@RequestBody ImAuditExportRequest exportRequest) {
        Map<String, Object> result = new HashMap<>();
        
        int rows = imAuditExportRequestService.updateImAuditExportRequest(exportRequest);
        
        if (rows > 0) {
            result.put("code", 200);
            result.put("msg", "审计导出请求修改成功");
            result.put("data", exportRequest);
        } else {
            result.put("code", 500);
            result.put("msg", "审计导出请求修改失败");
        }
        
        return result;
    }

    /**
     * 删除审计导出请求
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> remove(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        int rows = imAuditExportRequestService.deleteImAuditExportRequestById(id);
        
        if (rows > 0) {
            result.put("code", 200);
            result.put("msg", "审计导出请求删除成功");
        } else {
            result.put("code", 404);
            result.put("msg", "审计导出请求不存在");
        }
        
        return result;
    }

    /**
     * 批量删除审计导出请求
     */
    @DeleteMapping
    public Map<String, Object> remove(@RequestBody Long[] ids) {
        Map<String, Object> result = new HashMap<>();
        
        int rows = imAuditExportRequestService.deleteImAuditExportRequestByIds(ids);
        
        if (rows > 0) {
            result.put("code", 200);
            result.put("msg", "审计导出请求删除成功");
        } else {
            result.put("code", 500);
            result.put("msg", "审计导出请求删除失败");
        }
        
        return result;
    }

    /**
     * 删除指定时间之前的审计导出请求
     */
    @DeleteMapping("/before/{beforeTime}")
    public Map<String, Object> removeByBeforeTime(@PathVariable String beforeTime) {
        Map<String, Object> result = new HashMap<>();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime time = LocalDateTime.parse(beforeTime, formatter);
        
        int rows = imAuditExportRequestService.deleteImAuditExportRequestByBeforeTime(time);
        
        if (rows > 0) {
            result.put("code", 200);
            result.put("msg", "审计导出请求删除成功");
            result.put("data", rows);
        } else {
            result.put("code", 500);
            result.put("msg", "审计导出请求删除失败");
        }
        
        return result;
    }
}
