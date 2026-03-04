package com.ruoyi.im.service.impl;

import com.ruoyi.im.domain.ImUrlMetadata;
import com.ruoyi.im.mapper.ImUrlMetadataMapper;
import com.ruoyi.im.service.ImUrlMetadataService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.*;

/**
 * URL 元数据服务实现
 *
 * @author ruoyi
 */
@Service
public class ImUrlMetadataServiceImpl implements ImUrlMetadataService {

    private static final Logger log = LoggerFactory.getLogger(ImUrlMetadataServiceImpl.class);

    @Autowired
    private ImUrlMetadataMapper urlMetadataMapper;

    // 线程池用于异步抓取网页
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    // 缓存过期时间（7 天）
    private static final long CACHE_EXPIRE_DAYS = 7;

    // 请求超时时间（毫秒）
    private static final int TIMEOUT_MS = 10000;

    /**
     * 解析 URL 元数据
     */
    @Override
    public ImUrlMetadata parseUrl(String url) {
        // 1. 先查询缓存
        ImUrlMetadata cached = getByUrl(url);
        if (cached != null && isValidCache(cached)) {
            return cached;
        }

        // 2. 异步抓取网页
        return fetchUrlMetadata(url);
    }

    /**
     * 强制刷新 URL 元数据
     */
    @Override
    public ImUrlMetadata refreshUrl(String url) {
        return fetchUrlMetadata(url);
    }

    /**
     * 根据 URL 查询元数据
     */
    @Override
    public ImUrlMetadata getByUrl(String url) {
        try {
            return urlMetadataMapper.selectByUrl(url);
        } catch (Exception e) {
            log.error("查询 URL 元数据失败：url={}", url, e);
            return null;
        }
    }

    /**
     * 保存 URL 元数据
     */
    @Override
    public void save(ImUrlMetadata metadata) {
        try {
            if (metadata.getId() == null) {
                metadata.setCreateTime(LocalDateTime.now());
                metadata.setUpdateTime(LocalDateTime.now());
                metadata.setExpireTime(LocalDateTime.now().plusDays(CACHE_EXPIRE_DAYS));
                urlMetadataMapper.insertImUrlMetadata(metadata);
            } else {
                metadata.setUpdateTime(LocalDateTime.now());
                urlMetadataMapper.updateImUrlMetadata(metadata);
            }
        } catch (Exception e) {
            log.error("保存 URL 元数据失败：url={}", metadata.getUrl(), e);
        }
    }

    /**
     * 清理过期元数据
     */
    @Override
    public int cleanExpired() {
        try {
            return urlMetadataMapper.deleteExpiredMetadata(LocalDateTime.now());
        } catch (Exception e) {
            log.error("清理过期元数据失败", e);
            return 0;
        }
    }

    /**
     * 抓取网页元数据
     */
    private ImUrlMetadata fetchUrlMetadata(String url) {
        Future<ImUrlMetadata> future = executorService.submit(() -> {
            ImUrlMetadata metadata = new ImUrlMetadata();
            metadata.setUrl(url);
            metadata.setFetchStatus("PENDING");

            try {
                // 1. 发送 HTTP 请求获取网页内容
                Document doc = Jsoup.connect(url)
                        .timeout(TIMEOUT_MS)
                        .userAgent("Mozilla/5.0 (compatible; RuoYi-IM/1.0; +http://localhost)")
                        .followRedirects(true)
                        .get();

                // 2. 解析 Open Graph 元数据
                String title = doc.select("meta[property=og:title]").attr("content");
                if (title.isEmpty()) {
                    title = doc.select("title").first() != null ? doc.select("title").first().text() : "";
                }

                String description = doc.select("meta[property=og:description]").attr("content");
                if (description.isEmpty()) {
                    description = doc.select("meta[name=description]").attr("content");
                }

                String imageUrl = doc.select("meta[property=og:image]").attr("content");
                if (imageUrl.isEmpty()) {
                    // 尝试查找第一个图片
                    Element img = doc.select("img").first();
                    if (img != null && img.hasAttr("src")) {
                        imageUrl = img.attr("abs:src");
                    }
                }

                String siteName = doc.select("meta[property=og:site_name]").attr("content");
                if (siteName.isEmpty()) {
                    siteName = doc.select("meta[name=application-name]").attr("content");
                }

                String faviconUrl = doc.select("link[rel=icon]").attr("abs:href");
                if (faviconUrl.isEmpty()) {
                    faviconUrl = doc.select("link[rel=shortcut icon]").attr("abs:href");
                }
                if (faviconUrl.isEmpty()) {
                    // 默认 favicon
                    faviconUrl = getFaviconUrl(url);
                }

                String contentType = doc.select("meta[property=og:type]").attr("content");

                // 3. 填充数据
                metadata.setTitle(truncate(title, 200));
                metadata.setDescription(truncate(description, 500));
                metadata.setImageUrl(truncate(imageUrl, 500));
                metadata.setSiteName(truncate(siteName, 100));
                metadata.setFaviconUrl(truncate(faviconUrl, 500));
                metadata.setContentType(truncate(contentType, 50));
                metadata.setFetchStatus("SUCCESS");

                log.info("URL 元数据抓取成功：url={}, title={}", url, title);

            } catch (IOException e) {
                log.error("URL 元数据抓取失败：url={}", url, e);
                metadata.setFetchStatus("FAILED");
                metadata.setErrorMessage(e.getMessage());
            } catch (Exception e) {
                log.error("URL 元数据抓取异常：url={}", url, e);
                metadata.setFetchStatus("FAILED");
                metadata.setErrorMessage(e.getMessage());
            }

            // 4. 保存到数据库
            save(metadata);
            return metadata;
        });

        try {
            // 设置超时
            return future.get(15, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.error("URL 元数据抓取超时：url={}", url);
            future.cancel(true);
            return createFailedMetadata(url, "抓取超时");
        } catch (Exception e) {
            log.error("URL 元数据抓取异常：url={}", url, e);
            return createFailedMetadata(url, "抓取失败：" + e.getMessage());
        }
    }

    /**
     * 创建失败的元数据
     */
    private ImUrlMetadata createFailedMetadata(String url, String errorMsg) {
        ImUrlMetadata metadata = new ImUrlMetadata();
        metadata.setUrl(url);
        metadata.setFetchStatus("FAILED");
        metadata.setErrorMessage(errorMsg);
        save(metadata);
        return metadata;
    }

    /**
     * 判断缓存是否有效
     */
    private boolean isValidCache(ImUrlMetadata metadata) {
        if (metadata.getExpireTime() == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(metadata.getExpireTime());
    }

    /**
     * 截断字符串
     */
    private String truncate(String str, int maxLen) {
        if (str == null || str.length() <= maxLen) {
            return str;
        }
        return str.substring(0, maxLen);
    }

    /**
     * 获取默认 favicon URL
     */
    private String getFaviconUrl(String url) {
        try {
            java.net.URL urlObj = new java.net.URL(url);
            return urlObj.getProtocol() + "://" + urlObj.getHost() + "/favicon.ico";
        } catch (Exception e) {
            return "";
        }
    }
}
