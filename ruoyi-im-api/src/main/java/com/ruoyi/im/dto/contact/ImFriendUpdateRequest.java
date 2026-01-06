package com.ruoyi.im.dto.contact;

import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 好友更新请求
 *
 * @author ruoyi
 */
public class ImFriendUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 备注
     */
    @Size(max = 50, message = "备注长度不能超过50个字符")
    private String remark;

    /**
     * 好友分组
     */
    @Size(max = 20, message = "分组名称长度不能超过20个字符")
    private String groupName;

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
