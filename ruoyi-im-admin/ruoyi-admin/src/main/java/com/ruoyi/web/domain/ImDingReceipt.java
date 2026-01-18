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
 * DING回执对象 im_ding_receipt
 *
 * @author ruoyi
 * @date 2025-01-18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImDingReceipt extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 回执ID */
    private Long id;

    /** DING ID */
    private Long dingId;

    /** DING标题（关联字段，非数据库字段） */
    private String dingTitle;

    /** 接收者ID */
    private Long receiverId;

    /** 接收者名称（关联字段，非数据库字段） */
    private String receiverName;

    /** 接收者昵称（关联字段，非数据库字段） */
    private String receiverNickname;

    /** 阅读时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date readTime;

    /** 是否确认（0未确认 1已确认） */
    private Integer confirmed;

    /** 确认时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date confirmTime;

    /** 备注 */
    private String remark;

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
