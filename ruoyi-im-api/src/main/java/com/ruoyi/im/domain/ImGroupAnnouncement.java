package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 群组公告实体
 *
 * @author ruoyi
 */
@TableName("im_group_announcement")
@Data
@Schema(description = "群组公告")
public class ImGroupAnnouncement implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "公告ID")
    private Long id;

    @Schema(description = "群组ID")
    private Long groupId;

    @Schema(description = "发送者ID")
    private Long senderId;

    @Schema(description = "公告内容")
    private String content;

    @Schema(description = "公告类型: 1=普通公告, 2=系统公告, 3=活动通知")
    private Integer type;

    @Schema(description = "附件URL（图片、文件等）")
    private String attachmentUrl;

    @Schema(description = "是否置顶: 0=否, 1=是")
    private Integer isPinned;

    @Schema(description = "状态: 1=正常, 0=已撤回")
    private Integer status;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Schema(description = "过期时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;
}
