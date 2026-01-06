package com.ruoyi.im.domain;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审批表单数据实体
 *
 * 用于存储IM系统中的审批表单数据，记录审批实例中每个字段的详细信息
 * 包括字段名称、字段标签、字段值、字段类型等，用于结构化存储表单数据
 * 支持多种字段类型（文本、数字、日期、选择、文件）
 *
 * @author ruoyi
 */
@TableName("im_approval_form_data")
@Data
public class ImApprovalFormData implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据ID，主键，唯一标识表单数据记录
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 审批ID，关联到im_approval表，指定该表单数据所属的审批实例
     */
    private Long approvalId;

    /**
     * 字段名称，表单字段的标识名称
     */
    private String fieldName;

    /**
     * 字段标签，表单字段的显示标签
     */
    private String fieldLabel;

    /**
     * 字段值，表单字段的实际值
     */
    private String fieldValue;

    /**
     * 字段类型，支持TEXT文本/NUMBER数字/DATE日期/SELECT选择/FILE文件
     */
    private String fieldType;

    /**
     * 创建时间，表单数据的创建时间
     */
    private LocalDateTime createTime;
}
