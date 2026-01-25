package com.ruoyi.im.vo.ding;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DING已读用户VO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "DING已读用户VO")
public class DingReadUserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    @Schema(description = "用户ID")
    private Long userId;

    /** 用户名称 */
    @Schema(description = "用户名称")
    private String userName;

    /** 用户头像 */
    @Schema(description = "用户头像")
    private String userAvatar;

    /** 已读时间 */
    @Schema(description = "已读时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readTime;
}
