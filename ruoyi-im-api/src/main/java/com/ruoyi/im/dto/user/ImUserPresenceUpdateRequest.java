package com.ruoyi.im.dto.user;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 用户工作状态更新请求DTO
 * 用于更新：工作中、会议中、在线等状态
 *
 * @author ruoyi
 */
@Data
public class ImUserPresenceUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 工作状态字符串
     * 建议值：WORKING, BUSY, MEETING, LEAVE, ONLINE
     */
    @NotBlank(message = "状态内容不能为空")
    private String presenceStatus;
}
