package com.ruoyi.im.domain;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审批记录实体
 *
 * 用于存储IM系统中审批流程的操作记录，记录每个审批节点的处理情况
 * 包括审批人的操作类型（通过、驳回、转审、加签）、审批意见、操作时间等信息
 * 用于追溯审批流程的完整历史，支持审计和问题排查
 *
 * @author ruoyi
 */
@TableName("im_approval_record")
@Data
public class ImApprovalRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID，主键，唯一标识审批记录
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 审批ID，关联到im_approval表，标识所属的审批实例
     */
    private Long approvalId;

    /**
     * 节点ID，标识审批流程中的节点，用于定位审批在流程中的位置
     */
    private Long nodeId;

    /**
     * 审批人ID，执行审批操作的用户ID，关联到im_user表
     */
    private Long approverId;

    /**
     * 操作类型（APPROVE通过 REJECT驳回 TRANSFER转审 ADD_SIGN加签）
     * APPROVE: 审批人同意该审批，流程继续流转到下一节点
     * REJECT: 审批人驳回该审批，流程终止或返回到上一节点
     * TRANSFER: 审批人将审批转交给其他审批人处理
     * ADD_SIGN: 审批人添加额外的审批人进行会签
     */
    private String action;

    /**
     * 审批意见，审批人填写的意见或备注，用于说明审批决策的原因
     */
    private String comment;

    /**
     * 附件（JSON格式），审批操作相关的附件信息，以JSON字符串形式存储
     */
    private String attachments;

    /**
     * 操作时间，审批人执行审批操作的时间
     */
    private LocalDateTime actionTime;


}
