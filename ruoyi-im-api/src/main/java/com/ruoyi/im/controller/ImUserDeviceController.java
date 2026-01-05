package com.ruoyi.im.controller;

import com.ruoyi.im.controller.BaseController;
import com.ruoyi.im.annotation.RequirePermission;
import com.ruoyi.im.annotation.ImApiVersion;
import com.ruoyi.im.annotation.ImRateLimit;
import com.ruoyi.im.common.PageResult;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImUserDevice;
import com.ruoyi.im.dto.userdevice.ImUserDeviceQueryRequest;
import com.ruoyi.im.dto.userdevice.ImUserDeviceUpdateRequest;
import com.ruoyi.im.service.ImUserDeviceService;
import com.ruoyi.im.utils.ValidationUtils;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户设备Controller
 * 
 * @author ruoyi
 */
@Api(tags = "用户设备管理")
@RestController
@RequestMapping("/api/{version}/im/user-device")
@ImApiVersion(value = {"v1", "v2"}, description = "用户设备管理API，支持v1和v2版本")
public class ImUserDeviceController extends BaseController {
    
    @Autowired
    private ImUserDeviceService imUserDeviceService;

    /**
     * 查询用户设备列表
     */
    @ApiOperation("查询用户设备列表")
    @GetMapping("/list")
    @RequirePermission("im:user-device:list")
    public Result<PageResult<ImUserDevice>> list(ImUserDeviceQueryRequest request) {
        // 设置分页参数
        startPage(request.getPageNum(), request.getPageSize());
        
        // 将查询请求转换为实体查询对象
        ImUserDevice query = new ImUserDevice();
        query.setUserId(request.getUserId());
        query.setDeviceType(request.getDeviceType());
        query.setDeviceId(request.getDeviceId());
        
        // 查询用户设备列表
        List<ImUserDevice> userDevices = imUserDeviceService.selectList(query);
        
        return getDataTable(userDevices);
    }

    /**
     * 获取用户设备详细信息
     */
    @ApiOperation("获取用户设备详细信息")
    @GetMapping("/{id}")
    @RequirePermission("im:user-device:query")
    public Result<ImUserDevice> getInfo(@PathVariable("id") Long id) {
        ValidationUtils.validateId(id, "getInfo");
        ImUserDevice imUserDevice = imUserDeviceService.selectById(id);
        return Result.success(imUserDevice);
    }

    /**
     * 新增用户设备
     */
    @ApiOperation("新增用户设备")
    @PostMapping
    @RequirePermission("im:user-device:add")
    public Result<Void> add(@Valid @RequestBody ImUserDeviceUpdateRequest request, BindingResult bindingResult) {
        // 验证参数
        Result<Void> validationResult = handleValidationError(bindingResult);
        if (validationResult != null) {
            return validationResult;
        }
        
        ImUserDevice imUserDevice = new ImUserDevice();
        imUserDevice.setUserId(request.getUserId());
        imUserDevice.setDeviceType(request.getDeviceType());
        imUserDevice.setDeviceId(request.getDeviceId());
        imUserDevice.setDeviceName(request.getDeviceName());
        imUserDevice.setOsVersion(request.getOsVersion());
        imUserDevice.setAppVersion(request.getAppVersion());
        imUserDevice.setIpAddress(request.getIpAddress());
        imUserDevice.setLocation(request.getLocation());
        imUserDevice.setStatus(request.getStatus());
        imUserDevice.setCreateTime(LocalDateTime.now());
        imUserDevice.setUpdateTime(LocalDateTime.now());
        
        int result = imUserDeviceService.insert(imUserDevice);
        
        if (result > 0) {
            return Result.success();
        } else {
            return Result.error("用户设备创建失败");
        }
    }

    /**
     * 修改用户设备
     */
    @ApiOperation("修改用户设备")
    @PutMapping("/{id}")
    @RequirePermission("im:user-device:edit")
    public Result<Void> edit(@PathVariable("id") Long id,
                             @Valid @RequestBody ImUserDeviceUpdateRequest request, BindingResult bindingResult) {
        // 验证参数
        Result<Void> validationResult = handleValidationError(bindingResult);
        if (validationResult != null) {
            return validationResult;
        }
        
        ValidationUtils.validateId(id, "edit");
        
        ImUserDevice imUserDevice = imUserDeviceService.selectById(id);
        if (imUserDevice == null) {
            return Result.error(404, "用户设备不存在");
        }
        
        imUserDevice.setUserId(request.getUserId());
        imUserDevice.setDeviceType(request.getDeviceType());
        imUserDevice.setDeviceName(request.getDeviceName());
        imUserDevice.setOsVersion(request.getOsVersion());
        imUserDevice.setAppVersion(request.getAppVersion());
        imUserDevice.setIpAddress(request.getIpAddress());
        imUserDevice.setLocation(request.getLocation());
        imUserDevice.setStatus(request.getStatus());
        imUserDevice.setUpdateTime(LocalDateTime.now());
        
        int result = imUserDeviceService.update(imUserDevice);
        
        if (result > 0) {
            return Result.success();
        } else {
            return Result.error(500, "用户设备更新失败");
        }
    }

    /**
     * 删除用户设备
     */
    @ApiOperation("删除用户设备")
    @DeleteMapping("/{id}")
    @RequirePermission("im:user-device:remove")
    public Result<Void> remove(@PathVariable("id") Long id) {
        ValidationUtils.validateId(id, "remove");
        int result = imUserDeviceService.deleteById(id);
        
        if (result > 0) {
            return Result.success();
        } else {
            return Result.error("用户设备删除失败");
        }
    }
}