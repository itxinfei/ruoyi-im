package com.ruoyi.im.vo.audit;

import com.ruoyi.im.domain.ImAuditLog;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 审计日志分页列表VO
 */
@Data
public class AuditLogListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 日志列表 */
    private List<ImAuditLog> list;

    /** 总记录数 */
    private int total;

    /** 当前页码 */
    private Integer pageNum;

    /** 每页数量 */
    private Integer pageSize;
}
