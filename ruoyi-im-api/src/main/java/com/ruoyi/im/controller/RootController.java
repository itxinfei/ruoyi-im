package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 根路径控制器
 * 处理一些全局性的兼容接口
 *
 * @author ruoyi
 */
@RestController
public class RootController {

    /**
     * CSRF Token 冗余接口
     * 匹配路径 /csrf/token，解决前端 csrfManager.js 的 404 报错
     */
    @RequestMapping(value = "/csrf/token")
    public Result<Map<String, String>> getCsrfToken() {
        Map<String, String> map = new HashMap<>();
        // 前端 csrfManager.js 寻找的是 response.data.csrfToken
        map.put("csrfToken", "disabled-by-backend");
        map.put("parameterName", "_csrf");
        map.put("headerName", "X-CSRF-TOKEN");
        return Result.success(map);
    }
}
