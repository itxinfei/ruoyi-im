package com.ruoyi.web.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 群组公告实体
 *
 * 对应数据库表 im_group_announcement
 * 实际数据库字段：id, group_id, sender_id, content, type, attachment_url,
 * is_pinned, status, create_time, update_time, expire_time
 *
 * @author ruoyi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImGroupAnnouncement implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 公告ID，主键
     */
    private Long id;

    /**
     * 群组ID
     */
    private Long groupId;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 公告内容
     */
    private String content;

    /**
     * 公告类型: 1=普通公告, 2=系统公告, 3=活动通知
     */
    private Integer type;

    /**
     * 附件URL（图片、文件等）
     */
    private String attachmentUrl;

    /**
     * 是否置顶: 0否 1是
     */
    private Integer isPinned;

    /**
     * 状态: 1=正常, 0=已撤回
     */
    private Integer status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    // ========== 关联查询字段（非数据库字段） ==========

    /**
     * 发送者昵称
     */
    private String senderName;

    /**
     * 群组名称
     */
    private String groupName;

    /**
     * 类型名称
     */
    private String typeName;

}
