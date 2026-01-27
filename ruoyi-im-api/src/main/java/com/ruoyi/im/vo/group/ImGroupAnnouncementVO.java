package com.ruoyi.im.vo.group;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 群组公告VO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "群组公告信息")
public class ImGroupAnnouncementVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "公告ID")
    private Long id;

    @Schema(description = "群组ID")
    private Long groupId;

    @Schema(description = "群组名称")
    private String groupName;

    @Schema(description = "发送者ID")
    private Long senderId;

    @Schema(description = "发送者昵称")
    private String senderName;

    @Schema(description = "发送者头像")
    private String senderAvatar;

    @Schema(description = "公告内容")
    private String content;

    @Schema(description = "公告类型: 1=普通公告, 2=系统公告")
    private Integer type;

    @Schema(description = "附件URL")
    private String attachmentUrl;

    @Schema(description = "是否置顶")
    private Integer isPinned;

    @Schema(description = "状态: 1=正常, 0=已撤回")
    private Integer status;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "是否可以编辑（只有发送者和管理员可以编辑）")
    private Boolean canEdit = false;

    @Schema(description = "是否可以删除")
    private Boolean canDelete = false;

    @Schema(description = "过期时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    @Schema(description = "是否已过期")
    private Boolean isExpired;
}
