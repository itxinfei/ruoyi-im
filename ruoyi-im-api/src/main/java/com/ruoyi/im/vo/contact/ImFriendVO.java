package com.ruoyi.im.vo.contact;

import java.util.Date;

/**
 * 濂藉弸瑙嗗浘瀵硅薄 im_friend
 * 
 * @author ruoyi
 */
public class ImFriendVO
{
    /** 濂藉弸鍏崇郴ID */
    private Long id;

    /** 鐢ㄦ埛ID */
    private Long userId;

    /** 濂藉弸ID */
    private Long friendId;

    /** 鏄电О */
    private String nickname;

    /** 澶村儚 */
    private String avatar;

    /** 澶囨敞 */
    private String remark;

    /** 濂藉弸鐘舵€?*/
    private String status;

    /** 鍒涘缓鏃堕棿 */
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
