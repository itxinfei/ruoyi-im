package com.ruoyi.im.schedule;

import com.ruoyi.im.monitor.PerformanceMonitor;
import com.ruoyi.im.util.BusinessLogger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 性能数据定时任务
 * 
 * 定期收集和清理性能数据
 * 
 * @author ruoyi
 */
@Component
public class PerformanceDataSchedule {
    
    /**
     * 每天凌晨2点执行性能数据统计
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void collectPerformanceData() {
        Map<String, Map<String, Object>> allData = PerformanceMonitor.getAllPerformanceData();
        
        if (!allData.isEmpty()) {
            // 生成统计报告
            StringBuilder report = new StringBuilder();
            report.append("性能数据统计报告 (").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append(")\n");
            report.append(repeatChar('=', 80)).append("\n");
            
            for (Map.Entry<String, Map<String, Object>> entry : allData.entrySet()) {
                Map<String, Object> data = entry.getValue();
                report.append("接口: ").append(data.get("uri")).append("\n");
                report.append("方法: ").append(data.get("method")).append("\n");
                report.append("总请求数: ").append(data.get("totalRequests")).append("\n");
                report.append("成功请求数: ").append(data.get("successRequests")).append("\n");
                report.append("错误请求数: ").append(data.get("errorRequests")).append("\n");
                report.append("成功率: ").append(data.get("successRate")).append("\n");
                report.append("平均响应时间: ").append(data.get("avgResponseTime")).append("\n");
                report.append("最小响应时间: ").append(data.get("minResponseTime")).append("\n");
                report.append("最大响应时间: ").append(data.get("maxResponseTime")).append("\n");
                report.append(repeatChar('-', 80)).append("\n");
            }
            
            // 记录性能统计报告
            BusinessLogger.logSystemStatus("性能统计报告", report.toString());
            
            // 清理旧数据
            PerformanceMonitor.clearPerformanceData();
            BusinessLogger.logSystemStatus("性能数据清理", "已清理所有性能监控数据");
        } else {
            BusinessLogger.logSystemStatus("性能统计", "无性能数据需要统计");
        }
    }
    
    /**
     * 重复字符指定次数
     * 
     * @param ch 要重复的字符
     * @param count 重复次数
     * @return 重复后的字符串
     */
    private String repeatChar(char ch, int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(ch);
        }
        return sb.toString();
    }
}