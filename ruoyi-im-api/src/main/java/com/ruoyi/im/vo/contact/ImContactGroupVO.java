package com.ruoyi.im.vo.contact;

import java.io.Serializable;
import java.util.List;

/**
 * 联系人分组视图对象
 *
 * @author ruoyi
 */
public class ImContactGroupVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分组名称
     */
    private String groupName;

    /**
     * 分组数量
     */
    private Integer count;

    /**
     * 好友列表
     */
    private List<ImFriendVO> friends;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public List<ImFriendVO> getFriends() {
        return friends;
    }

    public void setFriends(List<ImFriendVO> friends) {
        this.friends = friends;
    }
}
