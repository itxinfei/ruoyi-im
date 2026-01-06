package com.ruoyi.im.vo.contact;

import java.util.List;

/**
 * 鑱旂郴浜虹兢缁勮鍥惧璞? * 
 * @author ruoyi
 */
public class ImContactGroupVO
{
    /** 缇ょ粍鍚嶇О锛堥€氬父鏄瀛楁瘝锛?*/
    private String groupName;

    /** 缇ょ粍涓殑濂藉弸鍒楄〃 */
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
