package com.ruoyi.im.domain;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审批节点实体
 *
 * 用于存储IM系统中的审批节点信息（模板节点定义）
 *
 * @author ruoyi
 */
@TableName("im_approval_node")
@Data
public class ImApprovalNode implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 节点ID，主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 模板ID，关联到im_approval_template表
     */
    @TableField("template_id")
    private Long templateId;

    /**
     * 节点编码
     */
    @TableField("node_key")
    private String nodeKey;

    /**
     * 节点名称
     */
    @TableField("node_name")
    private String nodeName;

    /**
     * 节点类型
     */
    @TableField("node_type")
    private String nodeType;

    /**
     * 审批人列表（JSON格式）
     */
    private String approvers;

    /**
     * 排序
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    // ==================== 以下字段为非数据库字段 ====================

    /**
     * 审批ID（非数据库字段，用于实例节点）
     */
    @TableField(exist = false)
    private Long approvalId;

    /**
     * 审批人ID列表（非数据库字段，使用approvers）
     */
    @TableField(exist = false)
    private String approverIds;

    /**
     * 审批方式（非数据库字段）
     */
    @TableField(exist = false)
    private String approveType;

    /**
     * 状态（非数据库字段）
     */
    @TableField(exist = false)
    private String status;

    /**
     * 处理时间（非数据库字段）
     */
    @TableField(exist = false)
    private LocalDateTime processTime;

    /**
     * 处理人ID（非数据库字段）
     */
    @TableField(exist = false)
    private Long processorId;

}
