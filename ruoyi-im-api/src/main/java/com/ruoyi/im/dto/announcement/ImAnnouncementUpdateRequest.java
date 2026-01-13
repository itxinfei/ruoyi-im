package com.ruoyi.im.dto.announcement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告更新请求
 *
 * @author ruoyi
 */
@Data
@Schema(description = "公告更新请求")
public class ImAnnouncementUpdateRequest {

    @Schema(description = "公告ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "公告标题")
    private String title;

    @Schema(description = "公告内容")
    private String content;

    @Schema(description = "优先级（1=普通 2=重要 3=紧急）")
    private Integer priority;

    @Schema(description = "过期时间")
    private LocalDateTime expiryTime;

    @Schema(description = "是否置顶")
    private Boolean isPinned;

    @Schema(description = "是否允许评论")
    private Boolean allowComment;

    @Schema(description = "备注")
    private String remark;
}
