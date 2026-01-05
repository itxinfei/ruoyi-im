package com.ruoyi.im.vo.contact;

import java.util.List;

/**
 * 联系人群组视图对象
 * 
 * @author ruoyi
 */
public class ImContactGroupVO
{
    /** 群组名称（通常是首字母） */
    private String groupName;

    /** 群组中的好友列表 */
    private List<ImFriendVO> friends;

    public void setGroupName(String groupName) 
    {
        this.groupName = groupName;
    }

    public String getGroupName() 
    {
        return groupName;
    }
    public void setFriends(List<ImFriendVO> friends) 
    {
        this.friends = friends;
    }

    public List<ImFriendVO> getFriends() 
    {
        return friends;
    }
}