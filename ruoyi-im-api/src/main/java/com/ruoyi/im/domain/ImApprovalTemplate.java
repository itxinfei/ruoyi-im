package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.ruoyi.im.common.BaseEntity;

import java.io.Serializable;

/**
 * 审批模板实体
 *
 * @author ruoyi
 */
@TableName("im_approval_template")
@Data
@EqualsAndHashCode(callSuper = true)
public class ImApprovalTemplate extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 模板ID，主键，唯一标识审批模板 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 模板名称，审批模板的显示名称，用于用户识别和选择 */
    private String name;

    /** 模板编码，审批模板的唯一编码，用于系统内部标识和引用 */
    private String code;

    /** 分类（请假/报销/采购/通用） */
    private String category;

    /** 模板描述，审批模板的详细说明，包括使用场景、注意事项等 */
    private String description;

    /** 表单结构（JSON格式），定义审批表单的字段、类型、验证规则等 */
    @TableField("form_schema")
    private String formSchema;

    /** 流程配置（JSON格式），定义审批流程的节点、审批人、流转规则等 */
    @TableField("flow_config")
    private String flowConfig;

    /** 模板图标，审批模板的图标URL，用于在界面上展示 */
    private String icon;

    /** 是否系统模板：0=否, 1=是 */
    @TableField("is_system")
    private Integer isSystem;

    /** 状态（ACTIVE启用 DISABLED停用） */
    private String status;

    /** 排序，模板的显示顺序，用于控制模板在列表中的排序 */
    @TableField("sort_order")
    private Integer sortOrder;

    /** 创建者 */
    @TableField("create_by")
    private String createBy;

    /** 更新者 */
    @TableField("update_by")
    private String updateBy;
}
