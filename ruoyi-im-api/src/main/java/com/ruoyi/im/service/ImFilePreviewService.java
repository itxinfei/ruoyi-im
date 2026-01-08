package com.ruoyi.im.service;

import com.ruoyi.im.vo.file.ImFilePreviewInfoVO;

/**
 * 文件预览服务接口
 *
 * @author ruoyi
 */
public interface ImFilePreviewService {

    /**
     * 获取文件预览信息
     * 根据文件类型返回不同的预览方式和URL
     *
     * @param fileId 文件ID
     * @param userId 用户ID
     * @return 预览信息
     */
    ImFilePreviewInfoVO getPreviewInfo(Long fileId, Long userId);

    /**
     * 生成图片缩略图
     *
     * @param fileId 文件ID
     * @param width 缩略图宽度
     * @param height 缩略图高度
     * @param userId 用户ID
     * @return 缩略图URL
     */
    String generateThumbnail(Long fileId, Integer width, Integer height, Long userId);

    /**
     * 获取文件预览URL（支持多种格式）
     *
     * @param fileId 文件ID
     * @param format 预览格式（image/pdf/html）
     * @param userId 用户ID
     * @return 预览URL
     */
    String getPreviewUrl(Long fileId, String format, Long userId);

    /**
     * 检查文件是否支持在线预览
     *
     * @param fileType 文件类型
     * @return 是否支持预览
     */
    Boolean isPreviewSupported(String fileType);
}
