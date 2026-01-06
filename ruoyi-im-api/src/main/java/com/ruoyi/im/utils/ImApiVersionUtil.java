package com.ruoyi.im.util;

import com.ruoyi.im.annotation.ImApiVersion;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

/**
 * IM API鐗堟湰鎺у埗宸ュ叿绫? * 
 * 鎻愪緵API鐗堟湰鐩稿叧鐨勫伐鍏锋柟娉曪紝鍖呮嫭鐗堟湰姣旇緝銆佸吋瀹规€ф鏌ャ€? * 鐗堟湰淇℃伅鑾峰彇绛夊姛鑳姐€? * 
 * @author ruoyi
 */
public class ImApiVersionUtil {

    /**
     * 鑾峰彇褰撳墠璇锋眰鐨凙PI鐗堟湰
     * 
     * @param request HTTP璇锋眰瀵硅薄
     * @return 褰撳墠API鐗堟湰鍙凤紝濡?"v1"
     */
    public static String getCurrentVersion(HttpServletRequest request) {
        return (String) request.getAttribute("apiVersion");
    }

    /**
     * 妫€鏌ユ寚瀹氱増鏈槸鍚﹀湪鏀寔鑼冨洿鍐?     * 
     * @param version 瑕佹鏌ョ殑鐗堟湰鍙?     * @param supportedVersions 鏀寔鐨勭増鏈垪琛?     * @return true琛ㄧず鐗堟湰鏀寔锛宖alse琛ㄧず涓嶆敮鎸?     */
    public static boolean isVersionSupported(String version, String[] supportedVersions) {
        if (version == null || supportedVersions == null) {
            return false;
        }
        return Arrays.asList(supportedVersions).contains(version);
    }

    /**
     * 妫€鏌ュ綋鍓嶈姹傜増鏈槸鍚︽敮鎸佹寚瀹氭敞瑙ｇ殑鐗堟湰瑕佹眰
     * 
     * @param request HTTP璇锋眰瀵硅薄
     * @param apiVersion 娉ㄨВ瀵硅薄
     * @return true琛ㄧず褰撳墠鐗堟湰鏀寔璇ユ敞瑙ｇ殑瑕佹眰锛宖alse琛ㄧず涓嶆敮鎸?     */
    public static boolean isVersionCompatible(HttpServletRequest request, ImApiVersion apiVersion) {
        if (apiVersion == null) {
            return true;
        }

        String currentVersion = getCurrentVersion(request);
        
        // 妫€鏌ュ叿浣撶増鏈垪琛?        if (apiVersion.value().length > 0) {
            return isVersionSupported(currentVersion, apiVersion.value());
        }
        
        // 妫€鏌ョ増鏈寖鍥?        String minVersion = apiVersion.min();
        String maxVersion = apiVersion.max();
        
        boolean minCheck = true;
        boolean maxCheck = true;
        
        if (!minVersion.isEmpty()) {
            minCheck = compareVersions(currentVersion, minVersion) >= 0;
        }
        
        if (!maxVersion.isEmpty()) {
            maxCheck = compareVersions(currentVersion, maxVersion) <= 0;
        }
        
        return minCheck && maxCheck;
    }

    /**
     * 姣旇緝涓や釜鐗堟湰鍙?     * 
     * @param version1 鐗堟湰鍙?
     * @param version2 鐗堟湰鍙?
     * @return 0琛ㄧず鐩哥瓑锛?琛ㄧずversion1澶т簬version2锛?1琛ㄧずversion1灏忎簬version2
     */
    public static int compareVersions(String version1, String version2) {
        if (version1 == null || version2 == null) {
            return 0;
        }

        // 鎻愬彇鐗堟湰鍙蜂腑鐨勬暟瀛楅儴鍒嗚繘琛屾瘮杈?        String v1 = version1.replaceAll("[^\\d]", "");
        String v2 = version2.replaceAll("[^\\d]", "");
        
        try {
            int num1 = Integer.parseInt(v1);
            int num2 = Integer.parseInt(v2);
            
            if (num1 == num2) {
                return 0;
            } else if (num1 > num2) {
                return 1;
            } else {
                return -1;
            }
        } catch (NumberFormatException e) {
            // 濡傛灉鏃犳硶瑙ｆ瀽鏁板瓧鐗堟湰锛屽垯浣跨敤瀛楃涓叉瘮杈?            return version1.compareTo(version2);
        }
    }

    /**
     * 鑾峰彇鐗堟湰淇℃伅鎻忚堪
     * 
     * @param supportedVersions 鏀寔鐨勭増鏈垪琛?     * @return 鐗堟湰鎻忚堪瀛楃涓?     */
    public static String getVersionDescription(String[] supportedVersions) {
        if (supportedVersions == null || supportedVersions.length == 0) {
            return "v1";
        }
        
        if (supportedVersions.length == 1) {
            return supportedVersions[0];
        }
        
        return String.join(", ", supportedVersions);
    }

    /**
     * 妫€鏌ユ槸鍚︿负鏈€鏂扮増鏈?     * 
     * @param currentVersion 褰撳墠鐗堟湰
     * @param supportedVersions 鏀寔鐨勭増鏈垪琛?     * @return true琛ㄧず褰撳墠鐗堟湰鏄渶鏂扮殑
     */
    public static boolean isLatestVersion(String currentVersion, String[] supportedVersions) {
        if (currentVersion == null || supportedVersions == null || supportedVersions.length == 0) {
            return true;
        }
        
        for (String version : supportedVersions) {
            if (compareVersions(version, currentVersion) > 0) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * 鑾峰彇鏀寔鐨勭増鏈垪琛?     * 
     * @return 鏀寔鐨勭増鏈垪琛?     */
    public static List<String> getSupportedVersions() {
        return Arrays.asList("v1", "v2", "v3");
    }
}
