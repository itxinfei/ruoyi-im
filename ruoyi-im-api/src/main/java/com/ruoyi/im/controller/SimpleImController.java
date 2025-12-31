package com.ruoyi.im.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * IM测试控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/im/test")
public class SimpleImController {

    @GetMapping("/status")
    public Map<String, Object> status() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "IM模块运行正常");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
}