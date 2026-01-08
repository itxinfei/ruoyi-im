package com.ruoyi.im.vo.schedule;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 日程参与人VO
 */
@Data
@Schema(description = "日程参与人")
public class ScheduleParticipantVO implements Serializable {

    @Schema(description = "参与人ID")
    private Long id;

    @Schema(description = "参与人ID")
    private Long userId;

    @Schema(description = "参与人姓名")
    private String userName;

    @Schema(description = "参与人头像")
    private String userAvatar;

    @Schema(description = "参与状态")
    private String status;

    @Schema(description = "参与状态名称")
    private String statusName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "回复时间")
    private LocalDateTime responseTime;
}
