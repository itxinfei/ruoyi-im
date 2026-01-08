package com.ruoyi.im.vo.workreport;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 工作日志点赞用户VO
 */
@Data
@Schema(description = "点赞用户")
public class WorkReportLikeUserVO implements Serializable {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户姓名")
    private String userName;

    @Schema(description = "用户头像")
    private String userAvatar;
}
