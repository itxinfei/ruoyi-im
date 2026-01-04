package com.ruoyi.im.controller;

import com.ruoyi.im.domain.ImAuditLog;
import com.ruoyi.im.service.ImAuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 审计日志控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/api/im/audit")
public class ImAuditLogController {

    @Autowired
    private ImAuditLogService imAuditLogService;

    /**
     * 查询审计日志列表
     */
    @GetMapping("/list")
    public Map<String, Object> list(@RequestParam(required = false) Long userId,
                                   @RequestParam(required = false) String operationType,
                                   @RequestParam(required = false) String targetType,
                                   @RequestParam(required = false) Long targetId,
                                   @RequestParam(required = false) String ipAddress,
                                   @RequestParam(defaultValue = "1") Integer pageNum,
                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        
        ImAuditLog query = new ImAuditLog();
        query.setUserId(userId);
        query.setOperationType(operationType);
        query.setTargetType(targetType);
        query.setTargetId(targetId);
        query.setIpAddress(ipAddress);
        
        List<ImAuditLog> allLogs = imAuditLogService.selectImAuditLogList(query);
        
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, allLogs.size());
        
        List<ImAuditLog> pagedLogs = start < allLogs.size() ? 
            allLogs.subList(start, end) : java.util.Collections.emptyList();
        
        Map<String, Object> pageResult = new HashMap<>();
        pageResult.put("rows", pagedLogs);
        pageResult.put("total", allLogs.size());
        pageResult.put("pageNum", pageNum);
        pageResult.put("pageSize", pageSize);
        
        result.put("code", 200);
        result.put("msg", "查询成功");
        result.put("data", pageResult);
        
        return result;
    }

    /**
     * 查询审计日志详细
     */
    @GetMapping("/{id}")
    public Map<String, Object> getInfo(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        ImAuditLog auditLog = imAuditLogService.selectImAuditLogById(id);
        
        if (auditLog != null) {
            result.put("code", 200);
            result.put("msg", "查询成功");
            result.put("data", auditLog);
        } else {
            result.put("code", 404);
            result.put("msg", "审计日志不存在");
        }
        
        return result;
    }

    /**
     * 根据用户ID查询审计日志列表
     */
    @GetMapping("/user/{userId}")
    public Map<String, Object> listByUserId(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        List<ImAuditLog> list = imAuditLogService.selectImAuditLogByUserId(userId);
        
        result.put("code", 200);
        result.put("msg", "查询成功");
        result.put("data", list);
        
        return result;
    }

    /**
     * 根据操作类型查询审计日志列表
     */
    @GetMapping("/operation/{operationType}")
    public Map<String, Object> listByOperationType(@PathVariable String operationType) {
        Map<String, Object> result = new HashMap<>();
        
        List<ImAuditLog> list = imAuditLogService.selectImAuditLogByOperationType(operationType);
        
        result.put("code", 200);
        result.put("msg", "查询成功");
        result.put("data", list);
        
        return result;
    }

    /**
     * 根据目标类型和目标ID查询审计日志列表
     */
    @GetMapping("/target")
    public Map<String, Object> listByTarget(@RequestParam String targetType, @RequestParam Long targetId) {
        Map<String, Object> result = new HashMap<>();
        
        List<ImAuditLog> list = imAuditLogService.selectImAuditLogByTarget(targetType, targetId);
        
        result.put("code", 200);
        result.put("msg", "查询成功");
        result.put("data", list);
        
        return result;
    }

    /**
     * 根据IP地址查询审计日志列表
     */
    @GetMapping("/ip/{ipAddress}")
    public Map<String, Object> listByIpAddress(@PathVariable String ipAddress) {
        Map<String, Object> result = new HashMap<>();
        
        List<ImAuditLog> list = imAuditLogService.selectImAuditLogByIpAddress(ipAddress);
        
        result.put("code", 200);
        result.put("msg", "查询成功");
        result.put("data", list);
        
        return result;
    }

    /**
     * 新增审计日志
     */
    @PostMapping
    public Map<String, Object> add(@RequestBody ImAuditLog auditLog) {
        Map<String, Object> result = new HashMap<>();
        
        int rows = imAuditLogService.insertImAuditLog(auditLog);
        
        if (rows > 0) {
            result.put("code", 200);
            result.put("msg", "审计日志添加成功");
            result.put("data", auditLog);
        } else {
            result.put("code", 500);
            result.put("msg", "审计日志添加失败");
        }
        
        return result;
    }

    /**
     * 记录审计日志
     */
    @PostMapping("/log")
    public Map<String, Object> logAudit(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        
        Long userId = params.get("userId") != null ? Long.valueOf(params.get("userId").toString()) : null;
        String operationType = params.get("operationType") != null ? params.get("operationType").toString() : null;
        String targetType = params.get("targetType") != null ? params.get("targetType").toString() : null;
        Long targetId = params.get("targetId") != null ? Long.valueOf(params.get("targetId").toString()) : null;
        String operationResult = params.get("operationResult") != null ? params.get("operationResult").toString() : null;
        String errorMessage = params.get("errorMessage") != null ? params.get("errorMessage").toString() : null;
        String ipAddress = params.get("ipAddress") != null ? params.get("ipAddress").toString() : null;
        String userAgent = params.get("userAgent") != null ? params.get("userAgent").toString() : null;
        
        int rows = imAuditLogService.logAudit(userId, operationType, targetType, targetId, operationResult, errorMessage, ipAddress, userAgent);
        
        if (rows > 0) {
            result.put("code", 200);
            result.put("msg", "审计日志记录成功");
        } else {
            result.put("code", 500);
            result.put("msg", "审计日志记录失败");
        }
        
        return result;
    }

    /**
     * 修改审计日志
     */
    @PutMapping
    public Map<String, Object> edit(@RequestBody ImAuditLog auditLog) {
        Map<String, Object> result = new HashMap<>();
        
        int rows = imAuditLogService.updateImAuditLog(auditLog);
        
        if (rows > 0) {
            result.put("code", 200);
            result.put("msg", "审计日志修改成功");
            result.put("data", auditLog);
        } else {
            result.put("code", 500);
            result.put("msg", "审计日志修改失败");
        }
        
        return result;
    }

    /**
     * 删除审计日志
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> remove(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        int rows = imAuditLogService.deleteImAuditLogById(id);
        
        if (rows > 0) {
            result.put("code", 200);
            result.put("msg", "审计日志删除成功");
        } else {
            result.put("code", 404);
            result.put("msg", "审计日志不存在");
        }
        
        return result;
    }

    /**
     * 批量删除审计日志
     */
    @DeleteMapping
    public Map<String, Object> remove(@RequestBody Long[] ids) {
        Map<String, Object> result = new HashMap<>();
        
        int rows = imAuditLogService.deleteImAuditLogByIds(ids);
        
        if (rows > 0) {
            result.put("code", 200);
            result.put("msg", "审计日志删除成功");
        } else {
            result.put("code", 500);
            result.put("msg", "审计日志删除失败");
        }
        
        return result;
    }

    /**
     * 批量删除指定时间之前的审计日志
     */
    @DeleteMapping("/before/{beforeTime}")
    public Map<String, Object> removeByBeforeTime(@PathVariable String beforeTime) {
        Map<String, Object> result = new HashMap<>();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime time = LocalDateTime.parse(beforeTime, formatter);
        
        int rows = imAuditLogService.deleteImAuditLogByBeforeTime(time);
        
        if (rows > 0) {
            result.put("code", 200);
            result.put("msg", "审计日志删除成功");
            result.put("data", rows);
        } else {
            result.put("code", 500);
            result.put("msg", "审计日志删除失败");
        }
        
        return result;
    }
}
