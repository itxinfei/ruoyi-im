package com.ruoyi.im.vo.config;

import lombok.Data;

/**
 * 黑名单用户VO
 */
@Data
public class BlockedUserVO {
    /** 用户ID */
    private Long userId;
    /** 用户昵称 */
    private String nickname;
    /** 用户头像 */
    private String avatar;
    /** 拉黑时间 */
    private String blockTime;
}
