package com.ruoyi.im.vo.document;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文档协作者VO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "文档协作者")
public class ImDocumentCollaboratorVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "协作者ID")
    private Long id;

    @Schema(description = "文档ID")
    private Long documentId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名称")
    private String userName;

    @Schema(description = "用户头像")
    private String userAvatar;

    @Schema(description = "协作权限")
    private String permission;

    @Schema(description = "在线状态")
    private String onlineStatus;

    @Schema(description = "光标位置")
    private String cursorPosition;

    @Schema(description = "选择范围")
    private String selectionRange;

    @Schema(description = "加入时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime joinTime;

    @Schema(description = "最后活跃时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastActiveTime;

    @Schema(description = "是否可编辑")
    private Boolean canEdit;

    @Schema(description = "是否可删除")
    private Boolean canRemove;
}
