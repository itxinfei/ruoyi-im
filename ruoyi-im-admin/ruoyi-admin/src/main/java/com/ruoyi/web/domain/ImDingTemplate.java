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
 * DING模板对象 im_ding_template
 *
 * @author ruoyi
 * @date 2025-01-18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImDingTemplate extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 模板ID */
    private Long id;

    /** 模板名称 */
    private String name;

    /** 模板内容 */
    private String content;

    /** 分类（MEETING会议 WORK工作 NOTICE通知 REMIND提醒） */
    private String category;

    /** 是否系统模板（0否 1是） */
    private Integer isSystem;

    /** 排序 */
    private Integer sortOrder;

    /** 状态（ACTIVE启用 DISABLED停用） */
    private String status;

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
