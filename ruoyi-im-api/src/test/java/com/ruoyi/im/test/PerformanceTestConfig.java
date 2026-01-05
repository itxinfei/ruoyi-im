package com.ruoyi.im.test;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 性能测试配置类
 * 
 * 提供性能测试所需的配置和工具方法，包括：
 * - 测试RestTemplate配置
 * - 性能报告输出路径设置
 * - 常用测试方法封装
 */
@TestConfiguration
@ActiveProfiles("test")
public class PerformanceTestConfig {

    // 测试基础URL
    public static final String TEST_BASE_URL = "http://localhost:8080/api";
    
    // 测试用户名和密码
    public static final String TEST_USERNAME = "admin";
    public static final String TEST_PASSWORD = "admin123";
    
    // 报告输出目录
    public static final String REPORT_OUTPUT_DIR = "target/performance-reports";
    
    // 日期时间格式器
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    
    /**
     * 创建测试RestTemplate
     */
    @Bean
    @Primary
    public TestRestTemplate createTestRestTemplate() {
        return new TestRestTemplate();
    }
    
    /**
     * 生成报告文件名
     * 
     * @param reportName 报告名称
     * @return 带时间戳的报告文件名
     */
    public static String generateReportFileName(String reportName) {
        String timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        return reportName + "_" + timestamp;
    }
    
    /**
     * 生成完整的报告文件路径
     * 
     * @param reportName 报告名称
     * @return 完整的报告文件路径
     */
    public static String generateReportFilePath(String reportName) {
        return REPORT_OUTPUT_DIR + "/" + generateReportFileName(reportName);
    }
    
    /**
     * 计算性能提升百分比
     * 
     * @param beforeValue 优化前的值
     * @param afterValue 优化后的值
     * @return 提升百分比（正值表示提升，负值表示下降）
     */
    public static double calculateImprovement(double beforeValue, double afterValue) {
        if (beforeValue == 0) {
            return 0;
        }
        return ((beforeValue - afterValue) / beforeValue) * 100;
    }
    
    /**
     * 计算响应时间提升百分比
     * 
     * @param beforeResponseTime 优化前的响应时间
     * @param afterResponseTime 优化后的响应时间
     * @return 响应时间提升百分比（正值表示提升，负值表示下降）
     */
    public static double calculateResponseTimeImprovement(double beforeResponseTime, double afterResponseTime) {
        if (beforeResponseTime == 0) {
            return 0;
        }
        return ((beforeResponseTime - afterResponseTime) / beforeResponseTime) * 100;
    }
    
    /**
     * 创建性能对比摘要
     * 
     * @param beforeResults 优化前的测试结果
     * @param afterResults 优化后的测试结果
     * @return 性能对比摘要
     */
    public static Map<String, Object> createPerformanceSummary(
            Map<String, PerformanceTestBase.PerformanceTestResult> beforeResults,
            Map<String, PerformanceTestBase.PerformanceTestResult> afterResults) {
        
        Map<String, Object> summary = new HashMap<>();
        
        // 计算总体指标
        double beforeTotalAvgResponseTime = 0;
        double afterTotalAvgResponseTime = 0;
        double beforeTotalSuccessRate = 0;
        double afterTotalSuccessRate = 0;
        double beforeTotalThroughput = 0;
        double afterTotalThroughput = 0;
        int apiCount = 0;
        
        for (String apiName : beforeResults.keySet()) {
            if (afterResults.containsKey(apiName)) {
                PerformanceTestBase.PerformanceTestResult beforeResult = beforeResults.get(apiName);
                PerformanceTestBase.PerformanceTestResult afterResult = afterResults.get(apiName);
                
                beforeTotalAvgResponseTime += beforeResult.getAvgResponseTime();
                afterTotalAvgResponseTime += afterResult.getAvgResponseTime();
                beforeTotalSuccessRate += beforeResult.getSuccessRate();
                afterTotalSuccessRate += afterResult.getSuccessRate();
                beforeTotalThroughput += beforeResult.getThroughput();
                afterTotalThroughput += afterResult.getThroughput();
                apiCount++;
            }
        }
        
        if (apiCount > 0) {
            beforeTotalAvgResponseTime /= apiCount;
            afterTotalAvgResponseTime /= apiCount;
            beforeTotalSuccessRate /= apiCount;
            afterTotalSuccessRate /= apiCount;
            beforeTotalThroughput /= apiCount;
            afterTotalThroughput /= apiCount;
        }
        
        // 计算提升百分比
        double responseTimeImprovement = calculateResponseTimeImprovement(beforeTotalAvgResponseTime, afterTotalAvgResponseTime);
        double successRateImprovement = calculateImprovement(beforeTotalSuccessRate, afterTotalSuccessRate);
        double throughputImprovement = calculateImprovement(beforeTotalThroughput, afterTotalThroughput);
        
        // 添加摘要数据
        summary.put("beforeTotalAvgResponseTime", beforeTotalAvgResponseTime);
        summary.put("afterTotalAvgResponseTime", afterTotalAvgResponseTime);
        summary.put("responseTimeImprovement", responseTimeImprovement);
        
        summary.put("beforeTotalSuccessRate", beforeTotalSuccessRate);
        summary.put("afterTotalSuccessRate", afterTotalSuccessRate);
        summary.put("successRateImprovement", successRateImprovement);
        
        summary.put("beforeTotalThroughput", beforeTotalThroughput);
        summary.put("afterTotalThroughput", afterTotalThroughput);
        summary.put("throughputImprovement", throughputImprovement);
        
        // 添加优化效果评估
        String performanceEvaluation;
        if (responseTimeImprovement > 20) {
            performanceEvaluation = "显著提升";
        } else if (responseTimeImprovement > 10) {
            performanceEvaluation = "明显提升";
        } else if (responseTimeImprovement > 0) {
            performanceEvaluation = "轻微提升";
        } else if (responseTimeImprovement > -10) {
            performanceEvaluation = "基本保持";
        } else {
            performanceEvaluation = "性能下降";
        }
        
        summary.put("performanceEvaluation", performanceEvaluation);
        
        return summary;
    }
    
    /**
     * 格式化百分比
     * 
     * @param value 百分比值
     * @return 格式化后的百分比字符串
     */
    public static String formatPercentage(double value) {
        return String.format("%.2f%%", value);
    }
    
    /**
     * 格式化数值
     * 
     * @param value 数值
     * @param decimals 小数位数
     * @return 格式化后的数值字符串
     */
    public static String formatNumber(double value, int decimals) {
        String format = "%." + decimals + "f";
        return String.format(format, value);
    }
}