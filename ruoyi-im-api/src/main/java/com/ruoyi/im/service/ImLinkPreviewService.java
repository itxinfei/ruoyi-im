package com.ruoyi.im.service;

import com.ruoyi.im.vo.link.ImLinkPreviewVO;

/**
 * 链接预览服务接口
 *
 * @author ruoyi
 */
public interface ImLinkPreviewService {

    /**
     * 解析链接获取预览信息
     *
     * @param url 链接地址
     * @return 预览信息，解析失败时返回基础信息（仅包含URL）
     */
    ImLinkPreviewVO parseLink(String url);

    /**
     * 判断是否为内网地址
     *
     * @param url 链接地址
     * @return true-内网地址，false-外网地址
     */
    boolean isInternalUrl(String url);

    /**
     * 生成缓存Key
     *
     * @param url 链接地址
     * @return 缓存Key
     */
    String generateCacheKey(String url);
}
