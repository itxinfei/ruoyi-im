package com.ruoyi.im.dto.videocall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 发起群组通话请求
 *
 * @author ruoyi
 */
@Data
@Schema(description = "发起群组通话请求")
public class VideoCallInitiateGroupRequest {

    @NotNull(message = "会话ID不能为空")
    @Schema(description = "会话ID", required = true)
    private Long conversationId;

    @NotBlank(message = "通话类型不能为空")
    @Schema(description = "通话类型", required = true, allowableValues = {"VIDEO", "AUDIO"}, example = "VIDEO")
    private String callType;

    @Min(value = 2, message = "最少支持2人同时通话")
    @Max(value = 9, message = "最多支持9人同时通话")
    @Schema(description = "最大参与者数", required = true, example = "9")
    private Integer maxParticipants;

    @NotEmpty(message = "邀请用户列表不能为空")
    @Schema(description = "邀请用户ID列表", required = true)
    private List<Long> invitedUserIds;
}