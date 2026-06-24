package com.ruoyi.im.vo.url;

import lombok.Data;

import java.io.Serializable;

/**
 * URL元数据VO
 *
 * @author ruoyi
 */
@Data
public class UrlMetadataVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * URL地址
     */
    private String url;

    /**
     * 页面标题
     */
    private String title;

    /**
     * 页面描述
     */
    private String description;

    /**
     * 图片URL
     */
    private String imageUrl;

    /**
     * 站点名称
     */
    private String siteName;

    /**
     * 网站图标URL
     */
    private String faviconUrl;

    /**
     * 内容类型
     */
    private String contentType;

    /**
     * 抓取状态
     */
    private String fetchStatus;

    /**
     * 错误信息（抓取失败时）
     */
    private String errorMessage;
}
