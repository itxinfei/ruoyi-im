package com.ruoyi.im.service.impl;

import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.service.ImLinkPreviewService;
import com.ruoyi.im.util.ImRedisUtil;
import com.ruoyi.im.vo.link.ImLinkPreviewVO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * 链接预览服务实现
 *
 * @author ruoyi
 */
@Service
public class ImLinkPreviewServiceImpl implements ImLinkPreviewService {

    private static final Logger log = LoggerFactory.getLogger(ImLinkPreviewServiceImpl.class);

    // 缓存相关
    private static final String LINK_CACHE_PREFIX = "im:link:";
    private static final int CACHE_DAYS = 7;

    // 解析相关
    private static final int TIMEOUT_MS = 5000;
    private static final String USER_AGENT = "Mozilla/5.0 (compatible; RuoYi-IM-LinkPreview/1.0)";
    private static final int MAX_REDIRECTS = 3;
    private static final int MAX_CONTENT_LENGTH = 1024 * 1024; // 1MB

    // 内网IP范围
    private static final String[] PRIVATE_PREFIXES = {
        "192.168.", "10.", "172.16.", "172.17.", "172.18.", "172.19.",
        "172.20.", "172.21.", "172.22.", "172.23.", "172.24.", "172.25.",
        "172.26.", "172.27.", "172.28.", "172.29.", "172.30.", "172.31.",
        "127.", "169.254.", "0.", "100.100.100."
    };

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 构造器注入RedisTemplate
     */
    public ImLinkPreviewServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public ImLinkPreviewVO parseLink(String url) {
        // 1. 检查是否为内网地址
        if (isInternalUrl(url)) {
            log.warn("拒绝访问内网地址: url={}", url);
            return buildFallbackVO(url);
        }

        // 2. 检查缓存
        String cacheKey = generateCacheKey(url);
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached instanceof ImLinkPreviewVO) {
                log.debug("链接预览缓存命中: url={}", url);
                return (ImLinkPreviewVO) cached;
            }
        } catch (Exception e) {
            log.warn("读取缓存失败: url={}, error={}", url, e.getMessage());
        }

        // 3. 解析链接
        ImLinkPreviewVO result = doParseLink(url);

        // 4. 缓存结果（即使解析失败也缓存，避免重复请求）
        try {
            redisTemplate.opsForValue().set(cacheKey, result, CACHE_DAYS, TimeUnit.DAYS);
            log.debug("缓存链接预览: url={}", url);
        } catch (Exception e) {
            log.warn("缓存失败: url={}, error={}", url, e.getMessage());
        }

        return result;
    }

    /**
     * 实际执行链接解析
     */
    private ImLinkPreviewVO doParseLink(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MS)
                    .followRedirects(true)
                    .maxBodySize(MAX_CONTENT_LENGTH)
                    .get();

            // 检查重定向后的URL是否为内网地址
            String actualUrl = doc.location();
            if (!actualUrl.equals(url) && isInternalUrl(actualUrl)) {
                log.warn("重定向到内网地址，拒绝访问: original={}, actual={}", url, actualUrl);
                return buildFallbackVO(url);
            }

            // 提取元数据
            ImLinkPreviewVO result = new ImLinkPreviewVO();
            result.setUrl(actualUrl);
            result.setTitle(extractTitle(doc));
            result.setDescription(extractDescription(doc));
            result.setImage(extractImage(doc, actualUrl));
            result.setFavicon(extractFavicon(doc, actualUrl));
            result.setSiteName(extractSiteName(doc));

            log.info("链接解析成功: url={}, title={}", actualUrl, result.getTitle());
            return result;

        } catch (java.net.SocketTimeoutException e) {
            log.warn("链接解析超时: url={}, error={}", url, e.getMessage());
            return buildFallbackVO(url);
        } catch (java.net.UnknownHostException e) {
            log.warn("域名解析失败: url={}, error={}", url, e.getMessage());
            return buildFallbackVO(url);
        } catch (java.io.IOException e) {
            log.warn("链接解析IO异常: url={}, error={}", url, e.getMessage());
            return buildFallbackVO(url);
        } catch (Exception e) {
            log.error("链接解析失败: url={}, error={}", url, e.getMessage(), e);
            return buildFallbackVO(url);
        }
    }

    /**
     * 提取标题
     */
    private String extractTitle(Document doc) {
        // 优先使用OG标题
        String ogTitle = doc.select("meta[property=og:title]").attr("content");
        if (StringUtils.hasText(ogTitle)) {
            return truncate(ogTitle, 100);
        }

        // 其次使用Twitter标题
        String twitterTitle = doc.select("meta[name=twitter:title]").attr("content");
        if (StringUtils.hasText(twitterTitle)) {
            return truncate(twitterTitle, 100);
        }

        // 最后使用title标签
        String title = doc.title();
        return StringUtils.hasText(title) ? truncate(title, 100) : "无法获取标题";
    }

    /**
     * 提取描述
     */
    private String extractDescription(Document doc) {
        // 优先使用OG描述
        String ogDesc = doc.select("meta[property=og:description]").attr("content");
        if (StringUtils.hasText(ogDesc)) {
            return truncate(ogDesc, 200);
        }

        // 其次使用Twitter描述
        String twitterDesc = doc.select("meta[name=twitter:description]").attr("content");
        if (StringUtils.hasText(twitterDesc)) {
            return truncate(twitterDesc, 200);
        }

        // 尝试meta description
        String metaDesc = doc.select("meta[name=description]").attr("content");
        if (StringUtils.hasText(metaDesc)) {
            return truncate(metaDesc, 200);
        }

        return null;
    }

    /**
     * 提取图片
     */
    private String extractImage(Document doc, String baseUrl) {
        // 优先使用OG图片
        String ogImage = doc.select("meta[property=og:image]").attr("content");
        if (StringUtils.hasText(ogImage)) {
            return makeAbsoluteUrl(ogImage, baseUrl);
        }

        // 其次使用Twitter图片
        String twitterImage = doc.select("meta[name=twitter:image]").attr("content");
        if (StringUtils.hasText(twitterImage)) {
            return makeAbsoluteUrl(twitterImage, baseUrl);
        }

        // 尝试找到第一张图片
        Elements images = doc.select("img[src]");
        for (Element img : images) {
            String src = img.attr("src");
            if (StringUtils.hasText(src) && !src.contains("favicon") && !src.contains("icon")) {
                String absoluteUrl = makeAbsoluteUrl(src, baseUrl);
                // 过滤掉明显的小图标
                if (!absoluteUrl.contains("icon") && !absoluteUrl.contains("logo")) {
                    return absoluteUrl;
                }
            }
        }

        return null;
    }

    /**
     * 提取favicon
     */
    private String extractFavicon(Document doc, String baseUrl) {
        // 尝试shortcut icon
        String shortcutIcon = doc.select("link[rel=shortcut icon]").attr("href");
        if (StringUtils.hasText(shortcutIcon)) {
            return makeAbsoluteUrl(shortcutIcon, baseUrl);
        }

        // 尝试icon
        String icon = doc.select("link[rel=icon]").attr("href");
        if (StringUtils.hasText(icon)) {
            return makeAbsoluteUrl(icon, baseUrl);
        }

        // 尝试apple-touch-icon
        String appleIcon = doc.select("link[rel=apple-touch-icon]").attr("href");
        if (StringUtils.hasText(appleIcon)) {
            return makeAbsoluteUrl(appleIcon, baseUrl);
        }

        // 默认favicon路径
        return baseUrl + "/favicon.ico";
    }

    /**
     * 提取站点名称
     */
    private String extractSiteName(Document doc) {
        String siteName = doc.select("meta[property=og:site_name]").attr("content");
        if (StringUtils.hasText(siteName)) {
            return truncate(siteName, 50);
        }
        return null;
    }

    /**
     * 将相对URL转换为绝对URL
     */
    private String makeAbsoluteUrl(String url, String baseUrl) {
        if (!StringUtils.hasText(url)) {
            return null;
        }

        try {
            if (url.startsWith("http://") || url.startsWith("https://")) {
                return url;
            }
            URL base = new URL(baseUrl);
            return new URL(base, url).toString();
        } catch (Exception e) {
            log.warn("转换绝对URL失败: url={}, baseUrl={}", url, baseUrl);
            return url;
        }
    }

    /**
     * 截断字符串
     */
    private String truncate(String str, int maxLength) {
        if (!StringUtils.hasText(str)) {
            return null;
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }

    /**
     * 构建降级VO（仅包含URL）
     */
    private ImLinkPreviewVO buildFallbackVO(String url) {
        ImLinkPreviewVO vo = new ImLinkPreviewVO();
        vo.setUrl(url);
        vo.setTitle("链接预览不可用");
        return vo;
    }

    @Override
    public boolean isInternalUrl(String url) {
        try {
            URL urlObj = new URL(url);
            String host = urlObj.getHost();

            // 检查localhost
            if ("localhost".equalsIgnoreCase(host) || host.startsWith("localhost.")) {
                return true;
            }

            // 解析IP地址
            InetAddress addr = InetAddress.getByName(host);
            String ip = addr.getHostAddress();

            // 检查内网IP范围
            for (String prefix : PRIVATE_PREFIXES) {
                if (ip.startsWith(prefix)) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            log.debug("判断内网地址异常: url={}, error={}", url, e.getMessage());
            return true; // 解析失败默认拒绝
        }
    }

    @Override
    public String generateCacheKey(String url) {
        String hash = DigestUtils.md5DigestAsHex(url.getBytes());
        return LINK_CACHE_PREFIX + hash;
    }
}
