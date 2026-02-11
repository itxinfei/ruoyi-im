package com.ruoyi.im.dto.meeting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 会议反馈请求
 *
 * @author ruoyi
 */
@Data
@Schema(description = "会议反馈请求")
public class MeetingFeedbackRequest {

    @NotNull(message = "反馈内容不能为空")
    @Schema(description = "反馈内容", required = true)
    private String feedback;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低为1分")
    @Max(value = 5, message = "评分最高为5分")
    @Schema(description = "评分（1-5分）", required = true, example = "5")
    private Integer rating;
}