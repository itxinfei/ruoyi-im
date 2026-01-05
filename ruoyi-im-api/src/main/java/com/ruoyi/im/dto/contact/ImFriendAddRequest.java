package com.ruoyi.im.dto.contact;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 * 添加好友请求对象
 * 
 * @author ruoyi
 */
public class ImFriendAddRequest
{
    /** 好友ID */
    @NotNull(message = "好友ID不能为空")
    @Positive(message = "好友ID必须为正数")
    private Long friendId;

    /** 备注 */
    private String remark;

    public void setFriendId(Long friendId) 
    {
        this.friendId = friendId;
    }

    public Long getFriendId() 
    {
        return friendId;
    }
    public void setRemark(String remark) 
    {
        this.remark = remark;
    }

    public String getRemark() 
    {
        return remark;
    }
}