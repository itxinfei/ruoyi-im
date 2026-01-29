package com.ruoyi.im.dto.group;

import com.ruoyi.im.dto.BasePageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 群组查询请求
 *
 * @author ruoyi
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ImGroupQueryRequest extends BasePageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 群组名称（模糊查询）
     */
    private String name;

    /**
     * 群组类型（PUBLIC公开群 PRIVATE私密群）
     */
    private String type;

    /**
     * 状态（NORMAL正常 DISMISSED已解散）
     */
    private String status;
}
