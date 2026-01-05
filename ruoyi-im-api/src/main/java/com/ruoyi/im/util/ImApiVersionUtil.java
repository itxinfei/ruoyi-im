package com.ruoyi.im.util;

import com.ruoyi.im.annotation.ImApiVersion;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

/**
 * IM API版本控制工具类
 * 
 * 提供API版本相关的工具方法，包括版本比较、兼容性检查、
 * 版本信息获取等功能。
 * 
 * @author ruoyi
 */
public class ImApiVersionUtil {

    /**
     * 获取当前请求的API版本
     * 
     * @param request HTTP请求对象
     * @return 当前API版本号，如 "v1"
     */
    public static String getCurrentVersion(HttpServletRequest request) {
        return (String) request.getAttribute("apiVersion");
    }

    /**
     * 检查指定版本是否在支持范围内
     * 
     * @param version 要检查的版本号
     * @param supportedVersions 支持的版本列表
     * @return true表示版本支持，false表示不支持
     */
    public static boolean isVersionSupported(String version, String[] supportedVersions) {
        if (version == null || supportedVersions == null) {
            return false;
        }
        return Arrays.asList(supportedVersions).contains(version);
    }

    /**
     * 检查当前请求版本是否支持指定注解的版本要求
     * 
     * @param request HTTP请求对象
     * @param apiVersion 注解对象
     * @return true表示当前版本支持该注解的要求，false表示不支持
     */
    public static boolean isVersionCompatible(HttpServletRequest request, ImApiVersion apiVersion) {
        if (apiVersion == null) {
            return true;
        }

        String currentVersion = getCurrentVersion(request);
        
        // 检查具体版本列表
        if (apiVersion.value().length > 0) {
            return isVersionSupported(currentVersion, apiVersion.value());
        }
        
        // 检查版本范围
        String minVersion = apiVersion.min();
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
     * 比较两个版本号
     * 
     * @param version1 版本号1
     * @param version2 版本号2
     * @return 0表示相等，1表示version1大于version2，-1表示version1小于version2
     */
    public static int compareVersions(String version1, String version2) {
        if (version1 == null || version2 == null) {
            return 0;
        }

        // 提取版本号中的数字部分进行比较
        String v1 = version1.replaceAll("[^\\d]", "");
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
            // 如果无法解析数字版本，则使用字符串比较
            return version1.compareTo(version2);
        }
    }

    /**
     * 获取版本信息描述
     * 
     * @param supportedVersions 支持的版本列表
     * @return 版本描述字符串
     */
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
     * 检查是否为最新版本
     * 
     * @param currentVersion 当前版本
     * @param supportedVersions 支持的版本列表
     * @return true表示当前版本是最新的
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
     * 获取支持的版本列表
     * 
     * @return 支持的版本列表
     */
    public static List<String> getSupportedVersions() {
        return Arrays.asList("v1", "v2", "v3");
    }
}