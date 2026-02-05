package com.ruoyi.im.vo.contact;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 联系人分组视图对象
 *
 * @author ruoyi
 */
@Data
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
     * 好友列表（前端使用 contacts 字段）
     */
    private List<ImFriendVO> contacts;

    /**
     * 好友列表（兼容旧代码）
     * @deprecated 使用 contacts 替代
     */
    @Deprecated
    private List<ImFriendVO> friends;
}
