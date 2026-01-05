package com.ruoyi.im.vo.contact;

import java.util.Date;

/**
 * 好友视图对象 im_friend
 * 
 * @author ruoyi
 */
public class ImFriendVO
{
    /** 好友关系ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 好友ID */
    private Long friendId;

    /** 昵称 */
    private String nickname;

    /** 头像 */
    private String avatar;

    /** 备注 */
    private String remark;

    /** 好友状态 */
    private String status;

    /** 创建时间 */
    private Date createTime;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }
    public void setUserId(Long userId) 
    {
        this.userId = userId;
    }

    public Long getUserId() 
    {
        return userId;
    }
    public void setFriendId(Long friendId) 
    {
        this.friendId = friendId;
    }

    public Long getFriendId() 
    {
        return friendId;
    }
    public void setNickname(String nickname) 
    {
        this.nickname = nickname;
    }

    public String getNickname() 
    {
        return nickname;
    }
    public void setAvatar(String avatar) 
    {
        this.avatar = avatar;
    }

    public String getAvatar() 
    {
        return avatar;
    }
    public void setRemark(String remark) 
    {
        this.remark = remark;
    }

    public String getRemark() 
    {
        return remark;
    }
    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }
    public Date getCreateTime() 
    {
        return createTime;
    }

    public void setCreateTime(Date createTime) 
    {
        this.createTime = createTime;
    }
}