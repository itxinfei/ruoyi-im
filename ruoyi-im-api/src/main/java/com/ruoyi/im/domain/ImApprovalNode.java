package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审批节点实体
 *
 * @author ruoyi
 */
@TableName("im_approval_node")
public class ImApprovalNode implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 节点ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 审批ID
     */
    private Long approvalId;

    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 节点类型：START开始/APPROVE审批/CC抄送/END结束
     */
    private String nodeType;

    /**
     * 审批人ID(JSON,多人用逗号分隔)
     */
    private String approverIds;

    /**
     * 审批方式：ANY一人即可/ALL所有人
     */
    private String approveType;

    /**
     * 序号
     */
    private Integer sortOrder;

    /**
     * 状态：PENDING待审批/APPROVED已通过/REJECTED已驳回/SKIPPED已跳过
     */
    private String status;

    /**
     * 处理时间
     */
    private LocalDateTime processTime;

    /**
     * 处理人ID
     */
    private Long processorId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getApprovalId() {
        return approvalId;
    }

    public void setApprovalId(Long approvalId) {
        this.approvalId = approvalId;
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

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
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
