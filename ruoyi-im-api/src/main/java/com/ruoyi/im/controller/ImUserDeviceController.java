package com.ruoyi.im.controller;

import com.ruoyi.im.domain.ImUserDevice;
import com.ruoyi.im.service.ImUserDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户设备控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/api/im/device")
public class ImUserDeviceController {

    @Autowired
    private ImUserDeviceService imUserDeviceService;

    /**
     * 查询用户设备列表
     */
    @GetMapping("/list")
    public Map<String, Object> list(@RequestParam(required = false) Long userId,
                                   @RequestParam(required = false) String deviceType,
                                   @RequestParam(required = false) String status) {
        Map<String, Object> result = new HashMap<>();
        
        ImUserDevice query = new ImUserDevice();
        query.setUserId(userId);
        query.setDeviceType(deviceType);
        query.setStatus(status);
        
        List<ImUserDevice> list = imUserDeviceService.selectImUserDeviceList(query);
        
        result.put("code", 200);
        result.put("msg", "查询成功");
        result.put("data", list);
        
        return result;
    }

    /**
     * 查询用户设备详细
     */
    @GetMapping("/{id}")
    public Map<String, Object> getInfo(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        ImUserDevice device = imUserDeviceService.selectImUserDeviceById(id);
        
        if (device != null) {
            result.put("code", 200);
            result.put("msg", "查询成功");
            result.put("data", device);
        } else {
            result.put("code", 404);
            result.put("msg", "用户设备不存在");
        }
        
        return result;
    }

    /**
     * 查询用户的所有设备
     */
    @GetMapping("/user/{userId}")
    public Map<String, Object> listByUserId(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        List<ImUserDevice> list = imUserDeviceService.selectImUserDeviceByUserId(userId);
        
        result.put("code", 200);
        result.put("msg", "查询成功");
        result.put("data", list);
        
        return result;
    }

    /**
     * 根据设备ID查询设备
     */
    @GetMapping("/deviceId/{deviceId}")
    public Map<String, Object> getByDeviceId(@PathVariable String deviceId) {
        Map<String, Object> result = new HashMap<>();
        
        ImUserDevice device = imUserDeviceService.selectImUserDeviceByDeviceId(deviceId);
        
        if (device != null) {
            result.put("code", 200);
            result.put("msg", "查询成功");
            result.put("data", device);
        } else {
            result.put("code", 404);
            result.put("msg", "设备不存在");
        }
        
        return result;
    }

    /**
     * 新增用户设备
     */
    @PostMapping
    public Map<String, Object> add(@RequestBody ImUserDevice device) {
        Map<String, Object> result = new HashMap<>();
        
        int rows = imUserDeviceService.insertImUserDevice(device);
        
        if (rows > 0) {
            result.put("code", 200);
            result.put("msg", "用户设备添加成功");
            result.put("data", device);
        } else {
            result.put("code", 500);
            result.put("msg", "用户设备添加失败");
        }
        
        return result;
    }

    /**
     * 注册设备
     */
    @PostMapping("/register")
    public Map<String, Object> registerDevice(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        
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
            result.put("code", 200);
            result.put("msg", "设备注册成功");
        } else {
            result.put("code", 500);
            result.put("msg", "设备注册失败");
        }
        
        return result;
    }

    /**
     * 更新设备活跃时间
     */
    @PostMapping("/updateActiveTime")
    public Map<String, Object> updateActiveTime(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        
        Long userId = Long.valueOf(params.get("userId").toString());
        String deviceId = params.get("deviceId").toString();
        
        int rows = imUserDeviceService.updateDeviceActiveTime(userId, deviceId);
        
        if (rows > 0) {
            result.put("code", 200);
            result.put("msg", "设备活跃时间更新成功");
        } else {
            result.put("code", 500);
            result.put("msg", "设备活跃时间更新失败");
        }
        
        return result;
    }

    /**
     * 更新设备状态
     */
    @PostMapping("/updateStatus")
    public Map<String, Object> updateStatus(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        
        Long userId = Long.valueOf(params.get("userId").toString());
        String deviceId = params.get("deviceId").toString();
        String status = params.get("status").toString();
        
        int rows = imUserDeviceService.updateDeviceStatus(userId, deviceId, status);
        
        if (rows > 0) {
            result.put("code", 200);
            result.put("msg", "设备状态更新成功");
        } else {
            result.put("code", 500);
            result.put("msg", "设备状态更新失败");
        }
        
        return result;
    }

    /**
     * 修改用户设备
     */
    @PutMapping
    public Map<String, Object> edit(@RequestBody ImUserDevice device) {
        Map<String, Object> result = new HashMap<>();
        
        int rows = imUserDeviceService.updateImUserDevice(device);
        
        if (rows > 0) {
            result.put("code", 200);
            result.put("msg", "用户设备修改成功");
            result.put("data", device);
        } else {
            result.put("code", 500);
            result.put("msg", "用户设备修改失败");
        }
        
        return result;
    }

    /**
     * 删除用户设备
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> remove(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        int rows = imUserDeviceService.deleteImUserDeviceById(id);
        
        if (rows > 0) {
            result.put("code", 200);
            result.put("msg", "用户设备删除成功");
        } else {
            result.put("code", 404);
            result.put("msg", "用户设备不存在");
        }
        
        return result;
    }

    /**
     * 批量删除用户设备
     */
    @DeleteMapping
    public Map<String, Object> remove(@RequestBody Long[] ids) {
        Map<String, Object> result = new HashMap<>();
        
        int rows = imUserDeviceService.deleteImUserDeviceByIds(ids);
        
        if (rows > 0) {
            result.put("code", 200);
            result.put("msg", "用户设备删除成功");
        } else {
            result.put("code", 500);
            result.put("msg", "用户设备删除失败");
        }
        
        return result;
    }

    /**
     * 删除用户的所有设备
     */
    @DeleteMapping("/user/{userId}")
    public Map<String, Object> removeByUserId(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        int rows = imUserDeviceService.deleteImUserDeviceByUserId(userId);
        
        if (rows > 0) {
            result.put("code", 200);
            result.put("msg", "用户设备删除成功");
        } else {
            result.put("code", 500);
            result.put("msg", "用户设备删除失败");
        }
        
        return result;
    }
}
