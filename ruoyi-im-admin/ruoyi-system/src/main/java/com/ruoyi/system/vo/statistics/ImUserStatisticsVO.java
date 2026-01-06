package com.ruoyi.system.vo.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户统计VO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "用户统计信息")
public class ImUserStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "发送消息数")
    private Integer sentMessages;

    @Schema(description = "接收消息数")
    private Integer receivedMessages;

    @Schema(description = "发送文件数")
    private Integer sentFiles;

    @Schema(description = "创建群组数")
    private Integer createdGroups;

    @Schema(description = "加入群组数")
    private Integer joinedGroups;

    @Schema(description = "最后登录时间")
    private String lastLoginTime;

    @Schema(description = "注册时间")
    private String registerTime;
}