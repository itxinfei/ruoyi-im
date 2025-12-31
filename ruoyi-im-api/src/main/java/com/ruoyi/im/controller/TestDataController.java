package com.ruoyi.im.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 测试数据管理控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping({"/im/test-data", "/api/im/test-data"})
public class TestDataController {
    
    @Autowired
    private TestDataService testDataService;
    
    /**
     * 生成测试数据
     */
    @PostMapping("/generate")
    public ResponseEntity<String> generateTestData() {
        try {
            testDataService.generateTestData();
            return ResponseEntity.ok("测试数据生成成功！");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("生成测试数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 清除测试数据
     */
    @DeleteMapping("/clear")
    public ResponseEntity<String> clearTestData() {
        try {
            testDataService.clearTestData();
            return ResponseEntity.ok("测试数据清除成功！");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("清除测试数据失败: " + e.getMessage());
        }
    }
}