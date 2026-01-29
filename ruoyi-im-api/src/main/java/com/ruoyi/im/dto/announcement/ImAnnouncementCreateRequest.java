package com.ruoyi.im.dto.announcement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告创建请求
 *
 * @author ruoyi
 */
@Data
@Schema(description = "公告创建请求")
public class ImAnnouncementCreateRequest {

    @Schema(description = "公告标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "公告标题不能为空")
    private String title;

    @Schema(description = "公告内容")
    private String content;

    @Schema(description = "公告类型（SYSTEM系统 DEPARTMENT部门 PROJECT项目）")
    private String announcementType = "SYSTEM";

    @Schema(description = "优先级（1=普通 2=重要 3=紧急）")
    private Integer priority = 1;

    @Schema(description = "过期时间")
    private LocalDateTime expiryTime;

    @Schema(description = "部门ID（部门公告时使用）")
    private Long departmentId;

    @Schema(description = "目标范围（ALL全部 DEPARTMENT部门指定 ROLE角色指定 USER用户指定）")
    private String targetType = "ALL";

    @Schema(description = "目标ID列表")
    private List<Long> targetIds;

    @Schema(description = "附件列表")
    private List<AttachmentInfo> attachments;

    @Schema(description = "是否置顶")
    private Boolean isPinned = false;

    @Schema(description = "是否允许评论")
    private Boolean allowComment = true;

    @Schema(description = "备注")
    private String remark;

    /**
     * 附件信息
     */
    @Data
    @Schema(description = "附件信息")
    public static class AttachmentInfo {
        @Schema(description = "附件ID")
        private Long id;

        @Schema(description = "文件名")
        private String name;

        @Schema(description = "文件URL")
        private String url;

        @Schema(description = "文件大小（字节）")
        private Long size;

        @Schema(description = "文件类型")
        private String fileType;
    }
}
