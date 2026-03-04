package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImUrlMetadata;

import java.util.Map;

/**
 * URL 元数据服务接口
 *
 * @author ruoyi
 */
public interface ImUrlMetadataService {

    /**
     * 解析 URL 元数据
     * 如果缓存中存在则返回缓存，否则抓取网页并缓存
     *
     * @param url URL 地址
     * @return URL 元数据
     */
    ImUrlMetadata parseUrl(String url);

    /**
     * 强制刷新 URL 元数据
     * 重新抓取网页内容
     *
     * @param url URL 地址
     * @return URL 元数据
     */
    ImUrlMetadata refreshUrl(String url);

    /**
     * 根据 URL 查询元数据（不抓取）
     *
     * @param url URL 地址
     * @return URL 元数据，不存在返回 null
     */
    ImUrlMetadata getByUrl(String url);

    /**
     * 保存 URL 元数据
     *
     * @param metadata URL 元数据
     */
    void save(ImUrlMetadata metadata);

    /**
     * 清理过期元数据
     *
     * @return 清理数量
     */
    int cleanExpired();
}
