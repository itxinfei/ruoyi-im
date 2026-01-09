package com.ruoyi.im.vo.group;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 群组视图对象
 *
 * @author ruoyi
 */
@Data
public class ImGroupVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 群组ID
     */
    private Long id;

    /**
     * 群组名称
     */
    private String name;

    /**
     * 群主用户ID
     */
    private Long ownerId;

    /**
     * 群主名称
     */
    private String ownerName;

    /**
     * 群公告
     */
    private String notice;

    /**
     * 群头像
     */
    private String avatar;

    /**
     * 状态（NORMAL正常 DISMISSED已解散）
     */
    private String status;

    /**
     * 成员数量
     */
    private Integer memberCount;

    /**
     * 成员数量限制
     */
    private Integer memberLimit;

    /**
     * 群组描述
     */
    private String description;

    /**
     * 群组类型（PUBLIC公开群 PRIVATE私密群）
     */
    private String type;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 当前用户在群中的角色（OWNER群主 ADMIN管理员 MEMBER普通成员）
     */
    private String myRole;

    /**
     * 当前用户是否被禁言
     */
    private Boolean isMuted;
}
