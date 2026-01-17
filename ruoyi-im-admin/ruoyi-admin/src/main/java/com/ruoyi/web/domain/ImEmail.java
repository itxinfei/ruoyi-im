package com.ruoyi.web.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 邮件对象 im_email
 *
 * @author ruoyi
 * @date 2025-01-15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImEmail extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** 发送者ID */
    private Long senderId;

    /** 发送者名称 */
    private String senderName;

    /** 发送者邮箱 */
    private String senderEmail;

    /** 接收者ID */
    private Long receiverId;

    /** 接收者名称（关联字段） */
    private String receiverName;

    /** 邮件主题 */
    private String subject;

    /** 邮件内容（HTML格式） */
    private String htmlContent;

    /** 纯文本内容 */
    private String textContent;

    /** 是否已读 */
    private Integer isRead;

    /** 是否已删除（软删除） */
    private Integer isDeleted;

    /** 是否星标 */
    private Integer isStarred;

    /** 邮件文件夹：INBOX/SENT/DRAFTS/SPAM/TRASH */
    private String folder;

    /** 附件数量 */
    private Integer attachmentCount;

    /** 发送时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date sendTime;

    /** 接收时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date receiveTime;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 请求参数 */
    private Map<String, Object> params;

    public Map<String, Object> getParams() {
        if (params == null) {
            params = new HashMap<>();
        }
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
}
