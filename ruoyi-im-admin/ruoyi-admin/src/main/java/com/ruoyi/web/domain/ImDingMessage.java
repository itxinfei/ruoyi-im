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
 * DING消息对象 im_ding_message
 *
 * @author ruoyi
 * @date 2025-01-15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImDingMessage extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** DING ID */
    private Long id;

    /** 发送者ID */
    private Long senderId;

    /** 发送者名称（关联字段） */
    private String senderName;

    /** DING内容 */
    private String content;

    /** DING类型（APP应用 SMS短信 PHONE电话） */
    private String dingType;

    /** 是否紧急（0否 1是） */
    private Integer isUrgent;

    /** 定时发送时间（为空则立即发送） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date scheduleTime;

    /** 实际发送时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date sendTime;

    /** 状态（DRAFT草稿 SENDING发送中 SENT已发送 CANCELLED已取消 FAILED失败） */
    private String status;

    /** 是否需要回执（0否 1是） */
    private Integer receiptRequired;

    /** 总接收人数 */
    private Integer totalCount;

    /** 已读人数 */
    private Integer readCount;

    /** 已确认人数 */
    private Integer confirmedCount;

    /** 附件URL */
    private String attachment;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

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
