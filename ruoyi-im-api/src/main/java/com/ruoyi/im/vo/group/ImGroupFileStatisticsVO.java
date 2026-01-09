package com.ruoyi.im.vo.group;

import lombok.Data;

import java.io.Serializable;

/**
 * 群组文件统计视图对象
 *
 * @author ruoyi
 */
@Data
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
}
