package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImUrlMetadata;
import com.ruoyi.im.service.ImUrlMetadataService;
import com.ruoyi.im.vo.url.UrlMetadataVO;
import org.springframework.web.bind.annotation.*;

/**
 * URL 元数据控制器
 * 提供链接预览功能
 *
 * @author ruoyi
 */

@RestController
@RequestMapping("/api/im/url")
public class ImUrlMetadataController {

    private final ImUrlMetadataService urlMetadataService;

    public ImUrlMetadataController(ImUrlMetadataService urlMetadataService) {
        this.urlMetadataService = urlMetadataService;
    }

    /**
     * 解析 URL 元数据
     * 根据 URL 解析网页的标题、描述、缩略图等元数据
     *
     * @param url 待解析的 URL 地址
     * @return URL 元数据
     */
    
    @GetMapping("/parse")
    public Result<UrlMetadataVO> parseUrl(@RequestParam String url) {
        try {
            // 验证 URL 格式
            if (!isValidUrl(url)) {
                return Result.fail("无效的 URL 格式");
            }

            ImUrlMetadata metadata = urlMetadataService.parseUrl(url);

            UrlMetadataVO result = new UrlMetadataVO();
            result.setUrl(metadata.getUrl());
            result.setTitle(metadata.getTitle());
            result.setDescription(metadata.getDescription());
            result.setImageUrl(metadata.getImageUrl());
            result.setSiteName(metadata.getSiteName());
            result.setFaviconUrl(metadata.getFaviconUrl());
            result.setContentType(metadata.getContentType());
            result.setFetchStatus(metadata.getFetchStatus());

            if ("FAILED".equals(metadata.getFetchStatus())) {
                result.setErrorMessage(metadata.getErrorMessage());
            }

            return Result.success(result);
        } catch (Exception e) {
            return Result.fail("解析失败：" + e.getMessage());
        }
    }

    /**
     * 刷新 URL 元数据
     * 强制重新抓取网页内容
     *
     * @param url 待刷新的 URL 地址
     * @return URL 元数据
     */
    
    @PostMapping("/refresh")
    public Result<UrlMetadataVO> refreshUrl(@RequestParam String url) {
        try {
            if (!isValidUrl(url)) {
                return Result.fail("无效的 URL 格式");
            }

            ImUrlMetadata metadata = urlMetadataService.refreshUrl(url);

            UrlMetadataVO result = new UrlMetadataVO();
            result.setUrl(metadata.getUrl());
            result.setTitle(metadata.getTitle());
            result.setDescription(metadata.getDescription());
            result.setImageUrl(metadata.getImageUrl());
            result.setSiteName(metadata.getSiteName());
            result.setFaviconUrl(metadata.getFaviconUrl());
            result.setFetchStatus(metadata.getFetchStatus());

            return Result.success(result);
        } catch (Exception e) {
            return Result.fail("刷新失败：" + e.getMessage());
        }
    }

    /**
     * 验证 URL 格式
     */
    private boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        try {
            java.net.URL parsed = new java.net.URL(url);
            // 只允许 http 和 https 协议
            return "http".equals(parsed.getProtocol()) || "https".equals(parsed.getProtocol());
        } catch (Exception e) {
            return false;
        }
    }
}

