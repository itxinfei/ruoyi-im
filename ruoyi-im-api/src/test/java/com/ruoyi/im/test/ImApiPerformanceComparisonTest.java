package com.ruoyi.im.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitJupiterTest;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * 性能对比测试主类
 * 
 * 用于运行API性能测试并比较优化前后的性能，包括：
 * - 运行优化前的API性能测试
 * - 运行优化后的API性能测试
 * - 生成性能对比报告
 * - 输出优化效果评估
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ImApiPerformanceComparisonTest {

    @Autowired
    private TestRestTemplate testRestTemplate;
    
    // 测试结果存储
    private Map<String, PerformanceTestBase.PerformanceTestResult> beforeOptimizationResults;
    private Map<String, PerformanceTestBase.PerformanceTestResult> afterOptimizationResults;
    
    // 测试报告输出目录
    private String reportOutputDir;
    
    @BeforeEach
    public void setUp() {
        // 设置报告输出目录
        reportOutputDir = PerformanceTestConfig.REPORT_OUTPUT_DIR;
        
        // 创建输出目录
        File outputDir = new File(reportOutputDir);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        
        // 初始化测试结果
        beforeOptimizationResults = new HashMap<>();
        afterOptimizationResults = new HashMap<>();
    }
    
    /**
     * 运行优化前的性能测试
     * 
     * 在API优化之前运行性能测试，并将结果保存到文件中
     */
    @Test
    public void runBeforeOptimizationTest() {
        System.out.println("开始运行优化前的性能测试...");
        
        // 运行优化前的性能测试
        ImUserApiPerformanceTest test = new ImUserApiPerformanceTest();
        test.setUp();
        
        try {
            // 运行测试
            beforeOptimizationResults = test.runAllTests();
            
            // 打印结果
            test.printResults(beforeOptimizationResults);
            
            // 保存结果到文件
            String jsonFileName = PerformanceTestConfig.generateReportFilePath("before-optimization-results");
            PerformanceReportGenerator.generateReport(beforeOptimizationResults, jsonFileName + ".json");
            System.out.println("优化前测试结果已保存到: " + jsonFileName + ".json");
            
            // 生成HTML报告
            PerformanceReportGenerator.generateHtmlReport(beforeOptimizationResults, jsonFileName + ".html");
            System.out.println("优化前测试报告已生成: " + jsonFileName + ".html");
            
        } catch (Exception e) {
            System.err.println("运行优化前性能测试失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            test.tearDown();
        }
    }
    
    /**
     * 运行优化后的性能测试
     * 
     * 在API优化之后运行性能测试，并将结果保存到文件中
     */
    @Test
    public void runAfterOptimizationTest() {
        System.out.println("开始运行优化后的性能测试...");
        
        // 运行优化后的性能测试
        ImUserApiPerformanceTest test = new ImUserApiPerformanceTest();
        test.setUp();
        
        try {
            // 运行测试
            afterOptimizationResults = test.runAllTests();
            
            // 打印结果
            test.printResults(afterOptimizationResults);
            
            // 保存结果到文件
            String jsonFileName = PerformanceTestConfig.generateReportFilePath("after-optimization-results");
            PerformanceReportGenerator.generateReport(afterOptimizationResults, jsonFileName + ".json");
            System.out.println("优化后测试结果已保存到: " + jsonFileName + ".json");
            
            // 生成HTML报告
            PerformanceReportGenerator.generateHtmlReport(afterOptimizationResults, jsonFileName + ".html");
            System.out.println("优化后测试报告已生成: " + jsonFileName + ".html");
            
        } catch (Exception e) {
            System.err.println("运行优化后性能测试失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            test.tearDown();
        }
    }
    
    /**
     * 生成性能对比报告
     * 
     * 比较优化前后的测试结果并生成对比报告
     */
    @Test
    public void generatePerformanceComparisonReport() {
        // 检查是否有测试结果
        if (beforeOptimizationResults.isEmpty()) {
            System.out.println("未找到优化前的测试结果，将尝试从文件加载...");
            loadBeforeOptimizationResults();
        }
        
        if (afterOptimizationResults.isEmpty()) {
            System.out.println("未找到优化后的测试结果，将尝试从文件加载...");
            loadAfterOptimizationResults();
        }
        
        // 检查结果是否都存在
        if (beforeOptimizationResults.isEmpty() || afterOptimizationResults.isEmpty()) {
            System.err.println("缺少必要的测试结果，无法生成对比报告");
            return;
        }
        
        System.out.println("开始生成性能对比报告...");
        
        try {
            // 生成HTML对比报告
            String reportFileName = PerformanceTestConfig.generateReportFilePath("performance-comparison");
            PerformanceReportGenerator.generateComparisonReport(
                beforeOptimizationResults, 
                afterOptimizationResults, 
                reportFileName + ".html");
            
            System.out.println("性能对比报告已生成: " + reportFileName + ".html");
            
            // 生成性能总结报告
            Map<String, Object> summary = PerformanceTestConfig.createPerformanceSummary(
                beforeOptimizationResults, 
                afterOptimizationResults);
            
            printPerformanceSummary(summary);
            
            // 保存性能总结到文件
            savePerformanceSummary(summary, reportFileName + "-summary.txt");
            
        } catch (Exception e) {
            System.err.println("生成性能对比报告失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 从文件加载优化前的测试结果
     */
    private void loadBeforeOptimizationResults() {
        try {
            File dir = new File(reportOutputDir);
            File[] files = dir.listFiles((d, name) -> name.startsWith("before-optimization-results") && name.endsWith(".json"));
            
            if (files != null && files.length > 0) {
                // 获取最新的文件
                File latestFile = files[0];
                for (File file : files) {
                    if (file.lastModified() > latestFile.lastModified()) {
                        latestFile = file;
                    }
                }
                
                // 加载文件
                Properties props = new Properties();
                try (FileInputStream fis = new FileInputStream(latestFile)) {
                    props.loadFromXML(fis);
                }
                
                // 解析结果
                for (String key : props.stringPropertyNames()) {
                    String value = props.getProperty(key);
                    if (StringUtils.hasText(value)) {
                        // 这里应该解析JSON并创建PerformanceTestResult对象
                        // 由于简化的原因，这里只是示例，实际需要完整的JSON解析
                        System.out.println("加载结果: " + key + " = " + value);
                    }
                }
                
                System.out.println("已从文件加载优化前的测试结果: " + latestFile.getName());
            }
        } catch (IOException e) {
            System.err.println("加载优化前测试结果失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 从文件加载优化后的测试结果
     */
    private void loadAfterOptimizationResults() {
        try {
            File dir = new File(reportOutputDir);
            File[] files = dir.listFiles((d, name) -> name.startsWith("after-optimization-results") && name.endsWith(".json"));
            
            if (files != null && files.length > 0) {
                // 获取最新的文件
                File latestFile = files[0];
                for (File file : files) {
                    if (file.lastModified() > latestFile.lastModified()) {
                        latestFile = file;
                    }
                }
                
                // 加载文件
                Properties props = new Properties();
                try (FileInputStream fis = new FileInputStream(latestFile)) {
                    props.loadFromXML(fis);
                }
                
                // 解析结果
                for (String key : props.stringPropertyNames()) {
                    String value = props.getProperty(key);
                    if (StringUtils.hasText(value)) {
                        // 这里应该解析JSON并创建PerformanceTestResult对象
                        // 由于简化的原因，这里只是示例，实际需要完整的JSON解析
                        System.out.println("加载结果: " + key + " = " + value);
                    }
                }
                
                System.out.println("已从文件加载优化后的测试结果: " + latestFile.getName());
            }
        } catch (IOException e) {
            System.err.println("加载优化后测试结果失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 打印性能总结
     */
    private void printPerformanceSummary(Map<String, Object> summary) {
        System.out.println("\n========== 性能优化效果总结 ==========");
        
        // 响应时间优化效果
        double beforeResponseTime = (double) summary.get("beforeTotalAvgResponseTime");
        double afterResponseTime = (double) summary.get("afterTotalAvgResponseTime");
        double responseTimeImprovement = (double) summary.get("responseTimeImprovement");
        
        System.out.println("平均响应时间: " + 
            PerformanceTestConfig.formatNumber(beforeResponseTime, 2) + " ms → " + 
            PerformanceTestConfig.formatNumber(afterResponseTime, 2) + " ms");
        System.out.println("响应时间优化: " + 
            PerformanceTestConfig.formatPercentage(responseTimeImprovement));
        
        // 成功率优化效果
        double beforeSuccessRate = (double) summary.get("beforeTotalSuccessRate");
        double afterSuccessRate = (double) summary.get("afterTotalSuccessRate");
        double successRateImprovement = (double) summary.get("successRateImprovement");
        
        System.out.println("成功率: " + 
            PerformanceTestConfig.formatNumber(beforeSuccessRate, 2) + "% → " + 
            PerformanceTestConfig.formatNumber(afterSuccessRate, 2) + "%");
        System.out.println("成功率优化: " + 
            PerformanceTestConfig.formatPercentage(successRateImprovement));
        
        // 吞吐量优化效果
        double beforeThroughput = (double) summary.get("beforeTotalThroughput");
        double afterThroughput = (double) summary.get("afterTotalThroughput");
        double throughputImprovement = (double) summary.get("throughputImprovement");
        
        System.out.println("吞吐量: " + 
            PerformanceTestConfig.formatNumber(beforeThroughput, 2) + " req/s → " + 
            PerformanceTestConfig.formatNumber(afterThroughput, 2) + " req/s");
        System.out.println("吞吐量优化: " + 
            PerformanceTestConfig.formatPercentage(throughputImprovement));
        
        // 优化效果评估
        String evaluation = (String) summary.get("performanceEvaluation");
        System.out.println("\n优化效果评估: " + evaluation);
        
        System.out.println("==========================================\n");
    }
    
    /**
     * 保存性能总结到文件
     */
    private void savePerformanceSummary(Map<String, Object> summary, String filePath) {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            Properties props = new Properties();
            
            for (Entry<String, Object> entry : summary.entrySet()) {
                props.setProperty(entry.getKey(), entry.getValue().toString());
            }
            
            props.storeToXML(fos, "性能优化总结报告");
            System.out.println("性能总结已保存到: " + filePath);
            
        } catch (IOException e) {
            System.err.println("保存性能总结失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}