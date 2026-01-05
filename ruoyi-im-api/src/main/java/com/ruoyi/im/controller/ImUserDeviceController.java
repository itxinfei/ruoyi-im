package com.ruoyi.im.controller;

import com.ruoyi.common.annotation.RequirePermission;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.im.annotation.SwaggerTag;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImUserDevice;
import com.ruoyi.im.service.ImUserDeviceService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 用户设备控制器
 * 
 * 提供用户设备管理的RESTful接口，包括设备列表查询、设备注册、
 * 设备状态更新、设备删除等功能。用于管理用户的多设备登录和设备状态。
 * 
 * @author ruoyi
 */
@Api(tags = "用户设备管理")
@RestController
@RequestMapping("/api/im/device")
public class ImUserDeviceController extends BaseController {

    @Autowired
    private ImUserDeviceService imUserDeviceService;

    /**
     * 查询用户设备列表
     * 
     * 根据指定条件查询用户设备列表，支持按用户ID、设备类型、设备状态进行过滤。
     * 返回所有符合条件的设备信息。
     * 
     * @param userId 用户ID，可选参数，用于过滤特定用户的设备
     * @param deviceType 设备类型，可选参数，如"WEB"、"MOBILE"、"DESKTOP"等
     * @param status 设备状态，可选参数，如"ACTIVE"、"INACTIVE"、"OFFLINE"等
     * @return 用户设备列表结果
     */
    @ApiOperation(value = "查询用户设备列表", notes = "根据条件查询用户设备列表，支持多条件过滤")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 401, message = "未授权访问"),
        @ApiResponse(code = 403, message = "权限不足")
    })
    @GetMapping("/list")
    @RequirePermission(value = "im:device:list", desc = "查看用户设备列表")
    public Result<List<ImUserDevice>> list(
            @ApiParam("用户ID，用于过滤特定用户的设备") @RequestParam(required = false) Long userId,
            @ApiParam("设备类型，如WEB、MOBILE、DESKTOP等") @RequestParam(required = false) String deviceType,
            @ApiParam("设备状态，如ACTIVE、INACTIVE、OFFLINE等") @RequestParam(required = false) String status) {
        ImUserDevice query = new ImUserDevice();
        query.setUserId(userId);
        query.setDeviceType(deviceType);
        query.setStatus(status);
        
        List<ImUserDevice> list = imUserDeviceService.selectImUserDeviceList(query);
        
        return Result.success(list, "查询成功");
    }

    /**
     * 查询用户设备详细
     * 
     * 根据设备ID获取用户设备的详细信息，包括设备的基本信息、
     * 设备状态、最后活跃时间等。
     * 
     * @param id 设备ID，用于标识要查询的具体设备
     * @return 用户设备详细信息
     */
    @ApiOperation(value = "查询用户设备详细", notes = "根据设备ID获取设备详细信息")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 404, message = "设备不存在"),
        @ApiResponse(code = 401, message = "未授权访问"),
        @ApiResponse(code = 403, message = "权限不足")
    })
    @GetMapping("/{id}")
    @RequirePermission(value = "im:device:get", desc = "查看设备详情")
    public Result<ImUserDevice> getInfo(@ApiParam("设备ID") @PathVariable Long id) {
        ImUserDevice device = imUserDeviceService.selectImUserDeviceById(id);
        
        if (device != null) {
            return Result.success(device, "查询成功");
        } else {
            return Result.error(404, "用户设备不存在");
        }
    }

    /**
     * 查询用户的所有设备
     * 
     * 获取指定用户的所有设备列表，包括活跃和不活跃的设备。
     * 用于查看用户的设备登录情况。
     * 
     * @param userId 用户ID，用于标识要查询设备的用户
     * @return 该用户的所有设备列表
     */
    @ApiOperation(value = "查询用户的所有设备", notes = "获取指定用户的所有设备列表")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 401, message = "未授权访问"),
        @ApiResponse(code = 403, message = "权限不足")
    })
    @GetMapping("/user/{userId}")
    @RequirePermission(value = "im:device:user:list", desc = "查看用户设备")
    public Result<List<ImUserDevice>> listByUserId(@ApiParam("用户ID") @PathVariable Long userId) {
        List<ImUserDevice> list = imUserDeviceService.selectImUserDeviceByUserId(userId);
        
        return Result.success(list, "查询成功");
    }

    /**
     * 根据设备ID查询设备
     */
    @GetMapping("/deviceId/{deviceId}")
    public Result<ImUserDevice> getByDeviceId(@PathVariable String deviceId) {
        ImUserDevice device = imUserDeviceService.selectImUserDeviceByDeviceId(deviceId);
        
        if (device != null) {
            return Result.success(device, "查询成功");
        } else {
            return Result.error(404, "设备不存在");
        }
    }

    /**
     * 新增用户设备
     */
    @PostMapping
    public Result<ImUserDevice> add(@RequestBody ImUserDevice device) {
        int rows = imUserDeviceService.insertImUserDevice(device);
        
        if (rows > 0) {
            return Result.success(device, "用户设备添加成功");
        } else {
            return Result.error(500, "用户设备添加失败");
        }
    }

    /**
     * 注册设备
     */
    @PostMapping("/register")
    public Result<Void> registerDevice(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        String deviceType = params.get("deviceType").toString();
        String deviceId = params.get("deviceId").toString();
        String deviceName = params.get("deviceName") != null ? params.get("deviceName").toString() : null;
        String osVersion = params.get("osVersion") != null ? params.get("osVersion").toString() : null;
        String appVersion = params.get("appVersion") != null ? params.get("appVersion").toString() : null;
        String ipAddress = params.get("ipAddress") != null ? params.get("ipAddress").toString() : null;
        String location = params.get("location") != null ? params.get("location").toString() : null;
        
        int rows = imUserDeviceService.registerDevice(userId, deviceType, deviceId, deviceName, osVersion, appVersion, ipAddress, location);
        
        if (rows > 0) {
            return Result.success("设备注册成功");
        } else {
            return Result.error(500, "设备注册失败");
        }
    }

    /**
     * 更新设备活跃时间
     */
    @PostMapping("/updateActiveTime")
    public Result<Void> updateActiveTime(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        String deviceId = params.get("deviceId").toString();
        
        int rows = imUserDeviceService.updateDeviceActiveTime(userId, deviceId);
        
        if (rows > 0) {
            return Result.success("设备活跃时间更新成功");
        } else {
            return Result.error(500, "设备活跃时间更新失败");
        }
    }

    /**
     * 更新设备状态
     */
    @PostMapping("/updateStatus")
    public Result<Void> updateStatus(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        String deviceId = params.get("deviceId").toString();
        String status = params.get("status").toString();
        
        int rows = imUserDeviceService.updateDeviceStatus(userId, deviceId, status);
        
        if (rows > 0) {
            return Result.success("设备状态更新成功");
        } else {
            return Result.error(500, "设备状态更新失败");
        }
    }

    /**
     * 修改用户设备
     */
    @PutMapping
    public Result<ImUserDevice> edit(@RequestBody ImUserDevice device) {
        int rows = imUserDeviceService.updateImUserDevice(device);
        
        if (rows > 0) {
            return Result.success(device, "用户设备修改成功");
        } else {
            return Result.error(500, "用户设备修改失败");
        }
    }

    /**
     * 删除用户设备
     */
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        int rows = imUserDeviceService.deleteImUserDeviceById(id);
        
        if (rows > 0) {
            return Result.success("用户设备删除成功");
        } else {
            return Result.error(404, "用户设备不存在");
        }
    }

    /**
     * 批量删除用户设备
     */
    @DeleteMapping
    public Result<Void> remove(@RequestBody Long[] ids) {
        int rows = imUserDeviceService.deleteImUserDeviceByIds(ids);
        
        if (rows > 0) {
            return Result.success("用户设备删除成功");
        } else {
            return Result.error(500, "用户设备删除失败");
        }
    }

    /**
     * 删除用户的所有设备
     */
    @DeleteMapping("/user/{userId}")
    public Result<Void> removeByUserId(@PathVariable Long userId) {
        int rows = imUserDeviceService.deleteImUserDeviceByUserId(userId);
        
        if (rows > 0) {
            return Result.success("用户设备删除成功");
        } else {
            return Result.error(500, "用户设备删除失败");
        }
    }
}
