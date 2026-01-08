package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审批表单数据实体
 *
 * @author ruoyi
 */
@TableName("im_approval_form_data")
@Data
public class ImApprovalFormData implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 数据ID，主键，唯一标识表单数据记录 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 审批ID，关联到im_approval表 */
    @TableField("approval_id")
    private Long approvalId;

    /** 字段名称，表单字段的标识名称 */
    @TableField("field_key")
    private String fieldKey;

    /** 字段值，表单字段的实际值 */
    @TableField("field_value")
    private String fieldValue;

    /** 字段类型，支持TEXT文本/NUMBER数字/DATE日期/SELECT选择/FILE文件 */
    @TableField("field_type")
    private String fieldType;

    /** 创建时间，表单数据的创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
