package com.ruoyi.im.controller;

import com.ruoyi.im.annotation.RateLimit;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.link.ImLinkPreviewRequest;
import com.ruoyi.im.service.ImLinkPreviewService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.link.ImLinkPreviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 链接预览控制器
 *
 * @author ruoyi
 */
@Tag(name = "链接预览", description = "链接预览解析接口")
@RestController
@RequestMapping("/api/im/link")
public class ImLinkPreviewController {

    private static final Logger log = LoggerFactory.getLogger(ImLinkPreviewController.class);

    private final ImLinkPreviewService linkPreviewService;

    /**
     * 构造器注入
     */
    public ImLinkPreviewController(ImLinkPreviewService linkPreviewService) {
        this.linkPreviewService = linkPreviewService;
    }

    /**
     * 解析链接预览
     *
     * @param request 链接预览请求
     * @return 链接预览信息
     */
    @Operation(summary = "解析链接预览", description = "获取URL的标题、描述和缩略图信息")
    @PostMapping("/preview")
    @RateLimit(key = "link_preview", time = 60, count = 20, limitType = RateLimit.LimitType.USER)
    public Result<ImLinkPreviewVO> previewLink(@Valid @RequestBody ImLinkPreviewRequest request) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            ImLinkPreviewVO result = linkPreviewService.parseLink(request.getUrl());
            log.info("链接解析成功: userId={}, url={}, title={}", userId, result.getUrl(), result.getTitle());
            return Result.success(result);
        } catch (Exception e) {
            log.error("链接解析失败: userId={}, url={}", userId, request.getUrl(), e);
            return Result.fail("解析失败，请稍后重试");
        }
    }
}
