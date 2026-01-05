package com.ruoyi.im.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 性能报告生成器
 * 
 * 用于生成性能测试报告，包括：
 * - JSON格式的详细报告
 * - HTML格式的可视化报告
 * - 对比优化前后的性能数据
 */
public class PerformanceReportGenerator {

    // 日期时间格式器
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // 对象映射器
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    static {
        // 配置对象映射器以美观的格式输出JSON
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }
    
    /**
     * 生成JSON格式的性能测试报告
     * 
     * @param results 性能测试结果
     * @param filePath 输出文件路径
     */
    public static void generateReport(Map<String, PerformanceTestBase.PerformanceTestResult> results, String filePath) {
        try {
            // 创建报告数据
            Map<String, Object> reportData = new HashMap<>();
            reportData.put("generateTime", LocalDateTime.now().format(DATE_TIME_FORMATTER));
            reportData.put("results", results);
            
            // 写入文件
            try (FileWriter writer = new FileWriter(filePath)) {
                objectMapper.writeValue(writer, reportData);
            }
        } catch (IOException e) {
            System.err.println("生成JSON报告失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 生成HTML格式的性能测试报告
     * 
     * @param results 性能测试结果
     * @param filePath 输出文件路径
     */
    public static void generateHtmlReport(Map<String, PerformanceTestBase.PerformanceTestResult> results, String filePath) {
        try {
            StringBuilder html = new StringBuilder();
            
            // HTML头部
            html.append("<!DOCTYPE html>");
            html.append("<html lang=\"zh-CN\">");
            html.append("<head>");
            html.append("<meta charset=\"UTF-8\">");
            html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
            html.append("<title>API性能测试报告</title>");
            html.append("<style>");
            html.append("body { font-family: Arial, sans-serif; margin: 20px; }");
            html.append("h1 { color: #2c3e50; }");
            html.append("h2 { color: #34495e; margin-top: 30px; }");
            html.append("table { border-collapse: collapse; width: 100%; margin-bottom: 30px; }");
            html.append("th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }");
            html.append("th { background-color: #f2f2f2; }");
            html.append("tr:nth-child(even) { background-color: #f9f9f9; }");
            html.append(".metrics { display: flex; justify-content: space-between; flex-wrap: wrap; }");
            html.append(".metric-card { border: 1px solid #ddd; border-radius: 4px; padding: 15px; margin-bottom: 20px; flex: 0 0 calc(33% - 10px); box-sizing: border-box; }");
            html.append(".metric-title { font-weight: bold; margin-bottom: 10px; color: #2c3e50; }");
            html.append(".metric-value { font-size: 1.2em; color: #3498db; }");
            html.append(".footer { margin-top: 40px; text-align: center; color: #7f8c8d; }");
            html.append("</style>");
            html.append("</head>");
            html.append("<body>");
            
            // 报告标题和时间
            html.append("<h1>API性能测试报告</h1>");
            html.append("<p>生成时间: ").append(LocalDateTime.now().format(DATE_TIME_FORMATTER)).append("</p>");
            
            // 总览指标
            html.append("<h2>总览指标</h2>");
            html.append("<div class=\"metrics\">");
            
            // 计算总请求数
            long totalRequests = 0;
            long totalSuccessRequests = 0;
            double totalAvgResponseTime = 0;
            int apiCount = 0;
            
            for (PerformanceTestBase.PerformanceTestResult result : results.values()) {
                totalRequests += result.getTotalRequests();
                totalSuccessRequests += result.getSuccessRequests();
                totalAvgResponseTime += result.getAvgResponseTime();
                apiCount++;
            }
            
            double overallAvgResponseTime = apiCount > 0 ? totalAvgResponseTime / apiCount : 0;
            double overallSuccessRate = totalRequests > 0 ? (double) totalSuccessRequests / totalRequests * 100 : 0;
            
            html.append("<div class=\"metric-card\">");
            html.append("<div class=\"metric-title\">总请求数</div>");
            html.append("<div class=\"metric-value\">").append(totalRequests).append("</div>");
            html.append("</div>");
            
            html.append("<div class=\"metric-card\">");
            html.append("<div class=\"metric-title\">平均响应时间</div>");
            html.append("<div class=\"metric-value\">").append(String.format("%.2f", overallAvgResponseTime)).append(" ms</div>");
            html.append("</div>");
            
            html.append("<div class=\"metric-card\">");
            html.append("<div class=\"metric-title\">成功率</div>");
            html.append("<div class=\"metric-value\">").append(String.format("%.2f", overallSuccessRate)).append("%</div>");
            html.append("</div>");
            
            html.append("</div>");
            
            // API性能详情
            html.append("<h2>API性能详情</h2>");
            
            for (Entry<String, PerformanceTestBase.PerformanceTestResult> entry : results.entrySet()) {
                String apiName = entry.getKey();
                PerformanceTestBase.PerformanceTestResult result = entry.getValue();
                
                html.append("<h3>").append(apiName).append(" API</h3>");
                html.append("<table>");
                html.append("<tr><th>指标</th><th>值</th></tr>");
                
                html.append("<tr><td>线程数</td><td>").append(result.getThreadCount()).append("</td></tr>");
                html.append("<tr><td>每个线程请求数</td><td>").append(result.getRequestCountPerThread()).append("</td></tr>");
                html.append("<tr><td>总请求数</td><td>").append(result.getTotalRequests()).append("</td></tr>");
                html.append("<tr><td>成功请求数</td><td>").append(result.getSuccessRequests()).append("</td></tr>");
                html.append("<tr><td>错误请求数</td><td>").append(result.getErrorRequests()).append("</td></tr>");
                html.append("<tr><td>总耗时</td><td>").append(result.getTotalTime()).append(" ms</td></tr>");
                html.append("<tr><td>最小响应时间</td><td>").append(result.getMinResponseTime()).append(" ms</td></tr>");
                html.append("<tr><td>最大响应时间</td><td>").append(result.getMaxResponseTime()).append(" ms</td></tr>");
                html.append("<tr><td>平均响应时间</td><td>").append(String.format("%.2f", result.getAvgResponseTime())).append(" ms</td></tr>");
                html.append("<tr><td>成功率</td><td>").append(String.format("%.2f", result.getSuccessRate())).append("%</td></tr>");
                html.append("<tr><td>吞吐量</td><td>").append(String.format("%.2f", result.getThroughput())).append(" req/s</td></tr>");
                
                html.append("</table>");
            }
            
            // 页脚
            html.append("<div class=\"footer\">");
            html.append("<p>Generated by Ruoyi IM API Performance Testing Framework</p>");
            html.append("</div>");
            
            html.append("</body>");
            html.append("</html>");
            
            // 写入文件
            try (FileWriter writer = new FileWriter(filePath)) {
                writer.write(html.toString());
            }
            
        } catch (IOException e) {
            System.err.println("生成HTML报告失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 生成性能对比报告（优化前后）
     * 
     * @param beforeResults 优化前的测试结果
     * @param afterResults 优化后的测试结果
     * @param filePath 输出文件路径
     */
    public static void generateComparisonReport(Map<String, PerformanceTestBase.PerformanceTestResult> beforeResults,
                                               Map<String, PerformanceTestBase.PerformanceTestResult> afterResults,
                                               String filePath) {
        try {
            StringBuilder html = new StringBuilder();
            
            // HTML头部
            html.append("<!DOCTYPE html>");
            html.append("<html lang=\"zh-CN\">");
            html.append("<head>");
            html.append("<meta charset=\"UTF-8\">");
            html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
            html.append("<title>API性能优化对比报告</title>");
            html.append("<style>");
            html.append("body { font-family: Arial, sans-serif; margin: 20px; }");
            html.append("h1 { color: #2c3e50; }");
            html.append("h2 { color: #34495e; margin-top: 30px; }");
            html.append("table { border-collapse: collapse; width: 100%; margin-bottom: 30px; }");
            html.append("th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }");
            html.append("th { background-color: #f2f2f2; }");
            html.append("tr:nth-child(even) { background-color: #f9f9f9; }");
            html.append(".improvement { color: #27ae60; font-weight: bold; }");
            html.append(".degradation { color: #e74c3c; font-weight: bold; }");
            html.append(".unchanged { color: #7f8c8d; }");
            html.append(".summary-card { border: 1px solid #ddd; border-radius: 4px; padding: 15px; margin-bottom: 20px; }");
            html.append(".summary-title { font-weight: bold; margin-bottom: 10px; color: #2c3e50; }");
            html.append(".summary-content { font-size: 1.1em; }");
            html.append(".footer { margin-top: 40px; text-align: center; color: #7f8c8d; }");
            html.append("</style>");
            html.append("</head>");
            html.append("<body>");
            
            // 报告标题和时间
            html.append("<h1>API性能优化对比报告</h1>");
            html.append("<p>生成时间: ").append(LocalDateTime.now().format(DATE_TIME_FORMATTER)).append("</p>");
            
            // 总体对比总结
            html.append("<h2>优化效果总结</h2>");
            html.append("<div class=\"summary-card\">");
            html.append("<div class=\"summary-title\">总体性能变化</div>");
            html.append("<div class=\"summary-content\">");
            
            // 计算总指标
            long beforeTotalRequests = 0;
            long afterTotalRequests = 0;
            double beforeTotalAvgResponseTime = 0;
            double afterTotalAvgResponseTime = 0;
            double beforeTotalSuccessRate = 0;
            double afterTotalSuccessRate = 0;
            int apiCount = 0;
            
            for (String apiName : beforeResults.keySet()) {
                if (afterResults.containsKey(apiName)) {
                    PerformanceTestBase.PerformanceTestResult beforeResult = beforeResults.get(apiName);
                    PerformanceTestBase.PerformanceTestResult afterResult = afterResults.get(apiName);
                    
                    beforeTotalRequests += beforeResult.getTotalRequests();
                    afterTotalRequests += afterResult.getTotalRequests();
                    beforeTotalAvgResponseTime += beforeResult.getAvgResponseTime();
                    afterTotalAvgResponseTime += afterResult.getAvgResponseTime();
                    beforeTotalSuccessRate += beforeResult.getSuccessRate();
                    afterTotalSuccessRate += afterResult.getSuccessRate();
                    apiCount++;
                }
            }
            
            double beforeOverallAvgResponseTime = apiCount > 0 ? beforeTotalAvgResponseTime / apiCount : 0;
            double afterOverallAvgResponseTime = apiCount > 0 ? afterTotalAvgResponseTime / apiCount : 0;
            double beforeOverallSuccessRate = apiCount > 0 ? beforeTotalSuccessRate / apiCount : 0;
            double afterOverallSuccessRate = apiCount > 0 ? afterTotalSuccessRate / apiCount : 0;
            
            // 计算改善百分比
            double responseTimeImprovement = beforeOverallAvgResponseTime > 0 ? 
                ((beforeOverallAvgResponseTime - afterOverallAvgResponseTime) / beforeOverallAvgResponseTime) * 100 : 0;
            double successRateImprovement = beforeOverallSuccessRate > 0 ? 
                ((afterOverallSuccessRate - beforeOverallSuccessRate) / beforeOverallSuccessRate) * 100 : 0;
            
            html.append("<p>平均响应时间: ").append(String.format("%.2f", beforeOverallAvgResponseTime)).append(" ms → ")
                .append(String.format("%.2f", afterOverallAvgResponseTime)).append(" ms ");
            
            if (responseTimeImprovement > 0) {
                html.append("<span class=\"improvement\">提升 ").append(String.format("%.2f", responseTimeImprovement)).append("%</span>");
            } else if (responseTimeImprovement < 0) {
                html.append("<span class=\"degradation\">下降 ").append(String.format("%.2f", -responseTimeImprovement)).append("%</span>");
            } else {
                html.append("<span class=\"unchanged\">无变化</span>");
            }
            html.append("</p>");
            
            html.append("<p>成功率: ").append(String.format("%.2f", beforeOverallSuccessRate)).append("% → ")
                .append(String.format("%.2f", afterOverallSuccessRate)).append("% ");
            
            if (successRateImprovement > 0) {
                html.append("<span class=\"improvement\">提升 ").append(String.format("%.2f", successRateImprovement)).append("%</span>");
            } else if (successRateImprovement < 0) {
                html.append("<span class=\"degradation\">下降 ").append(String.format("%.2f", -successRateImprovement)).append("%</span>");
            } else {
                html.append("<span class=\"unchanged\">无变化</span>");
            }
            html.append("</p>");
            
            html.append("</div>");
            html.append("</div>");
            
            // API对比详情
            html.append("<h2>API性能对比详情</h2>");
            
            for (String apiName : beforeResults.keySet()) {
                if (afterResults.containsKey(apiName)) {
                    PerformanceTestBase.PerformanceTestResult beforeResult = beforeResults.get(apiName);
                    PerformanceTestBase.PerformanceTestResult afterResult = afterResults.get(apiName);
                    
                    html.append("<h3>").append(apiName).append(" API</h3>");
                    html.append("<table>");
                    html.append("<tr><th>指标</th><th>优化前</th><th>优化后</th><th>变化</th></tr>");
                    
                    // 总请求数
                    html.append("<tr><td>总请求数</td><td>").append(beforeResult.getTotalRequests())
                        .append("</td><td>").append(afterResult.getTotalRequests()).append("</td><td>-</td></tr>");
                    
                    // 成功请求数
                    html.append("<tr><td>成功请求数</td><td>").append(beforeResult.getSuccessRequests())
                        .append("</td><td>").append(afterResult.getSuccessRequests()).append("</td><td>-</td></tr>");
                    
                    // 错误请求数
                    html.append("<tr><td>错误请求数</td><td>").append(beforeResult.getErrorRequests())
                        .append("</td><td>").append(afterResult.getErrorRequests()).append("</td><td>-</td></tr>");
                    
                    // 总耗时
                    html.append("<tr><td>总耗时</td><td>").append(beforeResult.getTotalTime()).append(" ms")
                        .append("</td><td>").append(afterResult.getTotalTime()).append(" ms</td><td>-</td></tr>");
                    
                    // 平均响应时间
                    double avgResponseTimeImprovement = beforeResult.getAvgResponseTime() > 0 ? 
                        ((beforeResult.getAvgResponseTime() - afterResult.getAvgResponseTime()) / beforeResult.getAvgResponseTime()) * 100 : 0;
                    String avgResponseTimeClass = avgResponseTimeImprovement > 0 ? "improvement" : 
                        (avgResponseTimeImprovement < 0 ? "degradation" : "unchanged");
                    String avgResponseTimeChange = avgResponseTimeImprovement > 0 ? "提升 " + String.format("%.2f", avgResponseTimeImprovement) + "%" : 
                        (avgResponseTimeImprovement < 0 ? "下降 " + String.format("%.2f", -avgResponseTimeImprovement) + "%" : "无变化");
                    
                    html.append("<tr><td>平均响应时间</td><td>").append(String.format("%.2f", beforeResult.getAvgResponseTime())).append(" ms")
                        .append("</td><td>").append(String.format("%.2f", afterResult.getAvgResponseTime())).append(" ms")
                        .append("</td><td><span class=\"").append(avgResponseTimeClass).append("\">").append(avgResponseTimeChange).append("</span></td></tr>");
                    
                    // 最小响应时间
                    html.append("<tr><td>最小响应时间</td><td>").append(beforeResult.getMinResponseTime()).append(" ms")
                        .append("</td><td>").append(afterResult.getMinResponseTime()).append(" ms</td><td>-</td></tr>");
                    
                    // 最大响应时间
                    html.append("<tr><td>最大响应时间</td><td>").append(beforeResult.getMaxResponseTime()).append(" ms")
                        .append("</td><td>").append(afterResult.getMaxResponseTime()).append(" ms</td><td>-</td></tr>");
                    
                    // 成功率
                    double successRateImprovement = beforeResult.getSuccessRate() > 0 ? 
                        ((afterResult.getSuccessRate() - beforeResult.getSuccessRate()) / beforeResult.getSuccessRate()) * 100 : 0;
                    String successRateClass = successRateImprovement > 0 ? "improvement" : 
                        (successRateImprovement < 0 ? "degradation" : "unchanged");
                    String successRateChange = successRateImprovement > 0 ? "提升 " + String.format("%.2f", successRateImprovement) + "%" : 
                        (successRateImprovement < 0 ? "下降 " + String.format("%.2f", -successRateImprovement) + "%" : "无变化");
                    
                    html.append("<tr><td>成功率</td><td>").append(String.format("%.2f", beforeResult.getSuccessRate())).append("%")
                        .append("</td><td>").append(String.format("%.2f", afterResult.getSuccessRate())).append("%")
                        .append("</td><td><span class=\"").append(successRateClass).append("\">").append(successRateChange).append("</span></td></tr>");
                    
                    // 吞吐量
                    double throughputImprovement = beforeResult.getThroughput() > 0 ? 
                        ((afterResult.getThroughput() - beforeResult.getThroughput()) / beforeResult.getThroughput()) * 100 : 0;
                    String throughputClass = throughputImprovement > 0 ? "improvement" : 
                        (throughputImprovement < 0 ? "degradation" : "unchanged");
                    String throughputChange = throughputImprovement > 0 ? "提升 " + String.format("%.2f", throughputImprovement) + "%" : 
                        (throughputImprovement < 0 ? "下降 " + String.format("%.2f", -throughputImprovement) + "%" : "无变化");
                    
                    html.append("<tr><td>吞吐量</td><td>").append(String.format("%.2f", beforeResult.getThroughput())).append(" req/s")
                        .append("</td><td>").append(String.format("%.2f", afterResult.getThroughput())).append(" req/s")
                        .append("</td><td><span class=\"").append(throughputClass).append("\">").append(throughputChange).append("</span></td></tr>");
                    
                    html.append("</table>");
                }
            }
            
            // 页脚
            html.append("<div class=\"footer\">");
            html.append("<p>Generated by Ruoyi IM API Performance Testing Framework</p>");
            html.append("</div>");
            
            html.append("</body>");
            html.append("</html>");
            
            // 写入文件
            try (FileWriter writer = new FileWriter(filePath)) {
                writer.write(html.toString());
            }
            
        } catch (IOException e) {
            System.err.println("生成对比报告失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}