package com.ruoyi.im.domain;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审批节点实体
 *
 * 用于存储IM系统中的审批节点信息，记录审批流程中每个节点的配置和处理状态
 * 包括节点名称、节点类型、审批人、审批方式、节点状态等，用于管理审批流程的流转
 * 支持多种节点类型（开始、审批、抄送、结束）和审批方式（一人即可、所有人）
 *
 * @author ruoyi
 */
@TableName("im_approval_node")
@Data
public class ImApprovalNode implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 节点ID，主键，唯一标识审批节点
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 审批ID，关联到im_approval表，指定该节点所属的审批实例
     */
    private Long approvalId;

    /**
     * 节点名称，审批节点的显示名称
     */
    private String nodeName;

    /**
     * 节点类型，支持START开始/APPROVE审批/CC抄送/END结束
     */
    private String nodeType;

    /**
     * 审批人ID，JSON格式，多人用逗号分隔
     */
    private String approverIds;

    /**
     * 审批方式，支持ANY一人即可/ALL所有人
     */
    private String approveType;

    /**
     * 序号，节点在审批流程中的顺序
     */
    private Integer sortOrder;

    /**
     * 状态，支持PENDING待审批/APPROVED已通过/REJECTED已驳回/SKIPPED已跳过
     */
    private String status;

    /**
     * 处理时间，节点被处理的时间
     */
    private LocalDateTime processTime;

    /**
     * 处理人ID，实际处理该节点的用户ID
     */
    private Long processorId;
}
