package com.ruoyi.im.schedule;

import com.ruoyi.im.monitor.PerformanceMonitor;
import com.ruoyi.im.utils.BusinessLogger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 鎬ц兘鏁版嵁瀹氭椂浠诲姟
 * 
 * 瀹氭湡鏀堕泦鍜屾竻鐞嗘€ц兘鏁版嵁
 * 
 * @author ruoyi
 */
@Component
public class PerformanceDataSchedule {
    
    /**
     * 姣忓ぉ鍑屾櫒2鐐规墽琛屾€ц兘鏁版嵁缁熻
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void collectPerformanceData() {
        Map<String, Map<String, Object>> allData = PerformanceMonitor.getAllPerformanceData();
        
        if (!allData.isEmpty()) {
            // 鐢熸垚缁熻鎶ュ憡
            StringBuilder report = new StringBuilder();
            report.append("鎬ц兘鏁版嵁缁熻鎶ュ憡 (").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append(")\n");
            report.append(repeatChar('=', 80)).append("\n");
            
            for (Map.Entry<String, Map<String, Object>> entry : allData.entrySet()) {
                Map<String, Object> data = entry.getValue();
                report.append("鎺ュ彛: ").append(data.get("uri")).append("\n");
                report.append("鏂规硶: ").append(data.get("method")).append("\n");
                report.append("鎬昏姹傛暟: ").append(data.get("totalRequests")).append("\n");
                report.append("鎴愬姛璇锋眰锟? ").append(data.get("successRequests")).append("\n");
                report.append("閿欒璇锋眰锟? ").append(data.get("errorRequests")).append("\n");
                report.append("鎴愬姛锟? ").append(data.get("successRate")).append("\n");
                report.append("骞冲潎鍝嶅簲鏃堕棿: ").append(data.get("avgResponseTime")).append("\n");
                report.append("鏈€灏忓搷搴旀椂锟? ").append(data.get("minResponseTime")).append("\n");
                report.append("鏈€澶у搷搴旀椂锟? ").append(data.get("maxResponseTime")).append("\n");
                report.append(repeatChar('-', 80)).append("\n");
            }
            
            // 璁板綍鎬ц兘缁熻鎶ュ憡
            BusinessLogger.logSystemStatus("鎬ц兘缁熻鎶ュ憡", report.toString());
            
            // 娓呯悊鏃ф暟锟?            PerformanceMonitor.clearPerformanceData();
            BusinessLogger.logSystemStatus("鎬ц兘鏁版嵁娓呯悊", "宸叉竻鐞嗘墍鏈夋€ц兘鐩戞帶鏁版嵁");
        } else {
            BusinessLogger.logSystemStatus("鎬ц兘缁熻", "鏃犳€ц兘鏁版嵁闇€瑕佺粺锟?);
        }
    }
    
    /**
     * 閲嶅瀛楃鎸囧畾娆℃暟
     * 
     * @param ch 瑕侀噸澶嶇殑瀛楃
     * @param count 閲嶅娆℃暟
     * @return 閲嶅鍚庣殑瀛楃锟?     */
    private String repeatChar(char ch, int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(ch);
        }
        return sb.toString();
    }
}
