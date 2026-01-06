package com.ruoyi.im.domain;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.im.common.BaseEntity;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审批模板实体
 *
 * 用于存储IM系统中的审批模板信息，定义审批流程的结构和规则
 * 包括模板名称、表单结构、流程配置等，为审批实例提供标准化的模板支持
 * 支持多种审批场景（请假、报销、采购等）和自定义审批流程
 *
 * @author ruoyi
 */
@TableName("im_approval_template")
@Data
public class ImApprovalTemplate extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 模板ID，主键，唯一标识审批模板
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 模板名称，审批模板的显示名称，用于用户识别和选择
     */
    private String name;

    /**
     * 模板编码，审批模板的唯一编码，用于系统内部标识和引用
     */
    private String code;

    /**
     * 分类（请假/报销/采购/通用）
     * 用于对审批模板进行分类管理，便于用户查找和使用
     */
    private String category;

    /**
     * 模板描述，审批模板的详细说明，包括使用场景、注意事项等
     */
    private String description;

    /**
     * 表单结构（JSON格式），定义审批表单的字段、类型、验证规则等
     * 以JSON字符串形式存储，支持动态表单配置
     */
    private String formSchema;

    /**
     * 流程配置（JSON格式），定义审批流程的节点、审批人、流转规则等
     * 以JSON字符串形式存储，支持复杂的审批流程配置
     */
    private String flowConfig;

    /**
     * 模板图标，审批模板的图标URL，用于在界面上展示
     */
    private String icon;

    /**
     * 是否系统模板，标识该模板是否为系统预置模板
     * true: 系统模板，由系统预置，不可删除
     * false: 自定义模板，由用户创建，可编辑和删除
     */
    private Boolean isSystem;

    /**
     * 状态（ACTIVE启用 DISABLED停用）
     * ACTIVE: 模板启用，用户可以使用该模板发起审批
     * DISABLED: 模板停用，用户无法使用该模板发起审批
     */
    private String status;

    /**
     * 排序，模板的显示顺序，用于控制模板在列表中的排序
     */
    private Integer sortOrder;
}
