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

    // ==================== Getters and Setters for compatibility ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public String getNodeKey() {
        return nodeKey;
    }

    public void setNodeKey(String nodeKey) {
        this.nodeKey = nodeKey;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getApprovers() {
        return approvers;
    }

    public void setApprovers(String approvers) {
        this.approvers = approvers;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public Long getApprovalId() {
        return approvalId;
    }

    public void setApprovalId(Long approvalId) {
        this.approvalId = approvalId;
    }

    public String getApproverIds() {
        return approverIds;
    }

    public void setApproverIds(String approverIds) {
        this.approverIds = approverIds;
    }

    public String getApproveType() {
        return approveType;
    }

    public void setApproveType(String approveType) {
        this.approveType = approveType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getProcessTime() {
        return processTime;
    }

    public void setProcessTime(LocalDateTime processTime) {
        this.processTime = processTime;
    }

    public Long getProcessorId() {
        return processorId;
    }

    public void setProcessorId(Long processorId) {
        this.processorId = processorId;
    }
}
