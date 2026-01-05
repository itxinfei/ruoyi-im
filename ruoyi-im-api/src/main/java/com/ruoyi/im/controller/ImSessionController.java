package com.ruoyi.im.controller;

import com.ruoyi.im.controller.BaseController;
import com.ruoyi.im.annotation.RequirePermission;
import com.ruoyi.im.annotation.ImApiVersion;
import com.ruoyi.im.annotation.ImRateLimit;
import com.ruoyi.im.common.PageResult;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImSession;
import com.ruoyi.im.dto.session.ImSessionQueryRequest;
import com.ruoyi.im.dto.session.ImSessionUpdateRequest;
import com.ruoyi.im.service.ImSessionService;
import com.ruoyi.im.utils.ValidationUtils;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 会话Controller
 * 
 * @author ruoyi
 */
@Api(tags = "会话管理")
@RestController
@RequestMapping("/api/{version}/im/session")
@ImApiVersion(value = {"v1", "v2"}, description = "会话管理API，支持v1和v2版本")
public class ImSessionController extends BaseController {
    
    @Autowired
    private ImSessionService imSessionService;

    /**
     * 查询会话列表
     */
    @ApiOperation("查询会话列表")
    @GetMapping("/list")
    @RequirePermission("im:session:list")
    public Result<PageResult<ImSession>> list(ImSessionQueryRequest request) {
        // 设置分页参数
        startPage(request.getPageNum(), request.getPageSize());
        
        // 查询会话列表
        List<ImSession> sessions = imSessionService.selectImSessionList(request);
        
        return getDataTable(sessions);
    }

    /**
     * 获取会话详细信息
     */
    @ApiOperation("获取会话详细信息")
    @GetMapping("/{id}")
    @RequirePermission("im:session:query")
    public Result<ImSession> getInfo(@PathVariable("id") Long id) {
        ValidationUtils.validateId(id, "getInfo");
        ImSession imSession = imSessionService.selectById(id);
        return Result.success(imSession);
    }

    /**
     * 新增会话
     */
    @ApiOperation("新增会话")
    @PostMapping
    @RequirePermission("im:session:add")
    public Result<Void> add(@Valid @RequestBody ImSessionUpdateRequest request, BindingResult bindingResult) {
        // 验证参数
        Result<Void> validationResult = handleValidationError(bindingResult);
        if (validationResult != null) {
            return validationResult;
        }
        
        ImSession imSession = new ImSession();
        imSession.setUserId(request.getUserId());
        imSession.setTargetId(request.getTargetId());
        imSession.setSessionType(request.getSessionType());
        imSession.setUnreadCount(0);
        imSession.setLastMessageTime(LocalDateTime.now());
        imSession.setCreateTime(LocalDateTime.now());
        imSession.setUpdateTime(LocalDateTime.now());
        
        int result = imSessionService.insert(imSession);
        
        if (result > 0) {
            return Result.success(201, "会话创建成功", null);
        } else {
            return Result.error("会话创建失败");
        }
    }

    /**
     * 修改会话
     */
    @ApiOperation("修改会话")
    @PutMapping("/{id}")
    @RequirePermission("im:session:edit")
    public Result<Void> edit(@PathVariable("id") Long id,
                             @Valid @RequestBody ImSessionUpdateRequest request, BindingResult bindingResult) {
        // 验证参数
        Result<Void> validationResult = handleValidationError(bindingResult);
        if (validationResult != null) {
            return validationResult;
        }
        
        ValidationUtils.validateId(id, "edit");
        
        ImSession imSession = imSessionService.selectById(id);
        if (imSession == null) {
            return Result.error(404, "会话不存在");
        }
        
        imSession.setUserId(request.getUserId());
        imSession.setTargetId(request.getTargetId());
        imSession.setSessionType(request.getSessionType());
        imSession.setUpdateTime(LocalDateTime.now());
        
        int result = imSessionService.update(imSession);
        
        if (result > 0) {
            return Result.success();
        } else {
            return Result.error(500, "会话更新失败");
        }
    }

    /**
     * 删除会话
     */
    @ApiOperation("删除会话")
    @DeleteMapping("/{id}")
    @RequirePermission("im:session:remove")
    public Result<Void> remove(@PathVariable("id") Long id) {
        ValidationUtils.validateId(id, "remove");
        int result = imSessionService.deleteById(id);
        
        if (result > 0) {
            return Result.success();
        } else {
            return Result.error("会话删除失败");
        }
    }
}