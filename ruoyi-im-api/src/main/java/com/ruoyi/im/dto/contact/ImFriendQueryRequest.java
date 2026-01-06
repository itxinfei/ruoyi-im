package com.ruoyi.im.dto.contact;

import com.ruoyi.im.dto.BasePageRequest;
import java.io.Serializable;

/**
 * 好友查询请求
 *
 * @author ruoyi
 */
public class ImFriendQueryRequest extends BasePageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 好友名称（模糊查询）
     */
    private String name;

    /**
     * 好友分组
     */
    private String groupName;

    /**
     * 状态（NORMAL正常 BLOCKED已拉黑）
     */
    private String status;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
