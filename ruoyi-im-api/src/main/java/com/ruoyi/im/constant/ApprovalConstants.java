package com.ruoyi.im.constant;

/**
 * 审批状态常量
 * 定义审批流程、节点、记录等状态常量
 *
 * @author ruoyi
 */
public final class ApprovalConstants {

    private ApprovalConstants() {
        // 防止实例化
    }

    // ========== 审批实例状态 ==========

    /** 审批状态：待审批 */
    public static final String STATUS_PENDING = "PENDING";

    /** 审批状态：进行中 */
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";

    /** 审批状态：已通过 */
    public static final String STATUS_APPROVED = "APPROVED";

    /** 审批状态：已拒绝 */
    public static final String STATUS_REJECTED = "REJECTED";

    /** 审批状态：已撤回 */
    public static final String STATUS_WITHDRAWN = "WITHDRAWN";

    /** 审批状态：已取消 */
    public static final String STATUS_CANCELLED = "CANCELLED";

    // ========== 审批节点类型 ==========

    /** 节点类型：审批节点 */
    public static final String NODE_TYPE_APPROVE = "APPROVE";

    /** 节点类型：条件分支 */
    public static final String NODE_TYPE_CONDITION = "CONDITION";

    /** 节点类型：抄送节点 */
    public static final String NODE_TYPE_CC = "CC";

    // ========== 审批类型 ==========

    /** 审批类型：任意一人审批即可 */
    public static final String APPROVE_TYPE_ANY = "ANY";

    /** 审批类型：需所有人审批 */
    public static final String APPROVE_TYPE_ALL = "ALL";

    /** 审批类型：按序审批 */
    public static final String APPROVE_TYPE_ORDER = "ORDER";

    // ========== 节点状态 ==========

    /** 节点状态：待审批 */
    public static final String NODE_STATUS_PENDING = "PENDING";

    /** 节点状态：已通过 */
    public static final String NODE_STATUS_APPROVED = "APPROVED";

    /** 节点状态：已拒绝 */
    public static final String NODE_STATUS_REJECTED = "REJECTED";

    /** 节点状态：已跳过 */
    public static final String NODE_STATUS_SKIPPED = "SKIPPED";

    // ========== 审批操作类型 ==========

    /** 操作：同意 */
    public static final String ACTION_AGREE = "AGREE";

    /** 操作：拒绝 */
    public static final String ACTION_REFUSE = "REFUSE";

    /** 操作：撤回 */
    public final static final String ACTION_WITHDRAW = "WITHDRAW";

    /** 操作：转办 */
    public static final String ACTION_TRANSFER = "TRANSFER";

    /** 操作：加签 */
    public static final String ACTION_ADD_SIGNER = "ADD_SIGNER";

    // ========== 默认值 ==========

    /** 默认节点名称 */
    public static final String DEFAULT_NODE_NAME = "审批";

    /** 默认排序序号 */
    public static final int DEFAULT_SORT_ORDER = 1;

    /** 默认表单字段类型 */
    public static final String DEFAULT_FIELD_TYPE = "TEXT";

    // ========== 审批模板状态 ==========

    /** 模板状态：启用 */
    public static final String TEMPLATE_STATUS_ACTIVE = "ACTIVE";

    /** 模板状态：禁用 */
    public static final String TEMPLATE_STATUS_DISABLED = "DISABLED";
}
