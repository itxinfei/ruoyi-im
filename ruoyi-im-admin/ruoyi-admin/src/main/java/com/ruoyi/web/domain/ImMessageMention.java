package com.ruoyi.web.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息@记录实体
 *
 * 对应数据库表 im_message_mention
 * 实际数据库字段：id, message_id, mentioned_user_id, mentioned_by, mention_type, is_read, create_time
 *
 * @author ruoyi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImMessageMention implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 消息ID */
    private Long messageId;

    /** 被@的用户ID */
    private Long mentionedUserId;

    /** @操作者ID */
    private Long mentionedBy;

    /** 提及类型：USER用户、ALL所有人 */
    private String mentionType;

    /** 是否已读：0未读、1已读 */
    private Integer isRead;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    // ========== 关联查询字段（非数据库字段） ==========

    /** 消息内容 */
    private String messageContent;

    /** 被@用户名 */
    private String mentionedUserName;

    /** 操作者用户名 */
    private String mentionedByUserName;
}
