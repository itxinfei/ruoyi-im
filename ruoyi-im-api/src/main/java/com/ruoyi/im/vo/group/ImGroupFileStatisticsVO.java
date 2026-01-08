package com.ruoyi.im.vo.group;

import java.io.Serializable;

/**
 * 群组文件统计视图对象
 *
 * @author ruoyi
 */
public class ImGroupFileStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总文件数
     */
    private Integer totalCount;

    /**
     * 总文件大小（字节）
     */
    private Long totalSize;

    /**
     * 格式化总文件大小
     */
    private String totalSizeFormat;

    /**
     * 图片文件数量
     */
    private Integer imageCount;

    /**
     * 视频文件数量
     */
    private Integer videoCount;

    /**
     * 音频文件数量
     */
    private Integer audioCount;

    /**
     * 文档文件数量
     */
    private Integer documentCount;

    /**
     * 其他文件数量
     */
    private Integer otherCount;

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(Long totalSize) {
        this.totalSize = totalSize;
    }

    public String getTotalSizeFormat() {
        return totalSizeFormat;
    }

    public void setTotalSizeFormat(String totalSizeFormat) {
        this.totalSizeFormat = totalSizeFormat;
    }

    public Integer getImageCount() {
        return imageCount;
    }

    public void setImageCount(Integer imageCount) {
        this.imageCount = imageCount;
    }

    public Integer getVideoCount() {
        return videoCount;
    }

    public void setVideoCount(Integer videoCount) {
        this.videoCount = videoCount;
    }

    public Integer getAudioCount() {
        return audioCount;
    }

    public void setAudioCount(Integer audioCount) {
        this.audioCount = audioCount;
    }

    public Integer getDocumentCount() {
        return documentCount;
    }

    public void setDocumentCount(Integer documentCount) {
        this.documentCount = documentCount;
    }

    public Integer getOtherCount() {
        return otherCount;
    }

    public void setOtherCount(Integer otherCount) {
        this.otherCount = otherCount;
    }
}
