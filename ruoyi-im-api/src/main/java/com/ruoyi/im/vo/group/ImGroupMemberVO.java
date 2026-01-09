package com.ruoyi.im.vo.group;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 群组成员视图对象
 *
 * @author ruoyi
 */
@Data
public class ImGroupMemberVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 成员ID
     */
    private Long id;

    /**
     * 群组ID
     */
    private Long groupId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 角色（OWNER群主 ADMIN管理员 MEMBER普通成员）
     */
    private String role;

    /**
     * 群内昵称
     */
    private String groupNickname;

    /**
     * 禁言止时间
     */
    private LocalDateTime muteEndTime;

    /**
     * 加入时间
     */
    private LocalDateTime joinedTime;

    /**
     * 是否被禁言
     */
    private Boolean isMuted;
}
