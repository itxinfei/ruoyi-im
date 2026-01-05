package com.ruoyi.im.controller;

import com.ruoyi.im.common.PageResult;
import com.ruoyi.im.common.Result;
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
public class ImAuditLogController extends BaseController {

    @Autowired
    private ImAuditLogService imAuditLogService;

    /**
     * 查询审计日志列表
     */
    @GetMapping("/list")
    public Result<PageResult<ImAuditLog>> list(@RequestParam(required = false) Long userId,
                                   @RequestParam(required = false) String operationType,
                                   @RequestParam(required = false) String targetType,
                                   @RequestParam(required = false) Long targetId,
                                   @RequestParam(required = false) String ipAddress) {
        startPage();
        ImAuditLog query = new ImAuditLog();
        query.setUserId(userId);
        query.setOperationType(operationType);
        query.setTargetType(targetType);
        query.setTargetId(targetId);
        query.setIpAddress(ipAddress);
        List<ImAuditLog> list = imAuditLogService.selectList(query);
        return getDataTable(list);
    }

    /**
     * 查询审计日志详细
     */
    @GetMapping("/{id}")
    public Result<ImAuditLog> getInfo(@PathVariable Long id) {
        ImAuditLog auditLog = imAuditLogService.selectById(id);
        
        if (auditLog != null) {
            return Result.success(auditLog);
        } else {
            return Result.error("审计日志不存在");
        }
    }

    /**
     * 根据用户ID查询审计日志列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<ImAuditLog>> listByUserId(@PathVariable Long userId) {
        List<ImAuditLog> list = imAuditLogService.selectImAuditLogByUserId(userId);
        
        return Result.success(list);
    }

    /**
     * 根据操作类型查询审计日志列表
     */
    @GetMapping("/operation/{operationType}")
    public Result<List<ImAuditLog>> listByOperationType(@PathVariable String operationType) {
        List<ImAuditLog> list = imAuditLogService.selectImAuditLogByOperationType(operationType);
        
        return Result.success(list);
    }

    /**
     * 根据目标类型和目标ID查询审计日志列表
     */
    @GetMapping("/target")
    public Result<List<ImAuditLog>> listByTarget(@RequestParam String targetType, @RequestParam Long targetId) {
        List<ImAuditLog> list = imAuditLogService.selectImAuditLogByTarget(targetType, targetId);
        
        return Result.success(list);
    }

    /**
     * 根据IP地址查询审计日志列表
     */
    @GetMapping("/ip/{ipAddress}")
    public Result<List<ImAuditLog>> listByIpAddress(@PathVariable String ipAddress) {
        List<ImAuditLog> list = imAuditLogService.selectImAuditLogByIpAddress(ipAddress);
        
        return Result.success(list);
    }

    /**
     * 新增审计日志
     */
    @PostMapping
    public Result<ImAuditLog> add(@RequestBody ImAuditLog auditLog) {
        int rows = imAuditLogService.insert(auditLog);
        
        if (rows > 0) {
            return Result.success(auditLog);
        } else {
            return Result.error("审计日志添加失败");
        }
    }

    /**
     * 记录审计日志
     */
    @PostMapping("/log")
    public Result<String> logAudit(@RequestBody Map<String, Object> params) {
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
            return Result.success("审计日志记录成功");
        } else {
            return Result.error("审计日志记录失败");
        }
    }

    /**
     * 修改审计日志
     */
    @PutMapping
    public Result<ImAuditLog> edit(@RequestBody ImAuditLog auditLog) {
        int rows = imAuditLogService.update(auditLog);
        
        if (rows > 0) {
            return Result.success(auditLog);
        } else {
            return Result.error("审计日志修改失败");
        }
    }

    /**
     * 删除审计日志
     */
    @DeleteMapping("/{id}")
    public Result<String> remove(@PathVariable Long id) {
        int rows = imAuditLogService.deleteById(id);
        
        if (rows > 0) {
            return Result.success("审计日志删除成功");
        } else {
            return Result.error("审计日志不存在");
        }
    }

    /**
     * 批量删除审计日志
     */
    @DeleteMapping
    public Result<String> remove(@RequestBody Long[] ids) {
        int rows = imAuditLogService.deleteByIds(ids);
        
        if (rows > 0) {
            return Result.success("审计日志删除成功");
        } else {
            return Result.error("审计日志删除失败");
        }
    }

    /**
     * 批量删除指定时间之前的审计日志
     */
    @DeleteMapping("/before/{beforeTime}")
    public Result<Integer> removeByBeforeTime(@PathVariable String beforeTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime time = LocalDateTime.parse(beforeTime, formatter);
        
        int rows = imAuditLogService.deleteImAuditLogByBeforeTime(time);
        
        return Result.success(rows);
    }
}
