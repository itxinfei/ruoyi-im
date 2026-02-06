package com.ruoyi.im.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息搜索请求DTO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "消息搜索请求")
public class ImMessageSearchRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "会话ID（可选，为空则搜索所有会话）")
    private Long conversationId;

    @Schema(description = "搜索关键词（支持模糊匹配）")
    private String keyword;

    @Schema(description = "消息类型：TEXT文本 FILE文件 IMAGE图片 VOICE语音 VIDEO视频 REPLY回复 FORWARD转发")
    private String messageType;

    @Schema(description = "发送者ID（可选，筛选特定发送者的消息）")
    private Long senderId;

    @Schema(description = "开始时间（格式：yyyy-MM-dd HH:mm:ss）")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "结束时间（格式：yyyy-MM-dd HH:mm:ss）")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @Schema(description = "页码，从1开始", defaultValue = "1")
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", defaultValue = "20")
    @NotNull(message = "每页数量不能为空")
    @Min(value = 1, message = "每页最少1条")
    @Max(value = 100, message = "每页最多100条")
    private Integer pageSize = 20;

    @Schema(description = "是否包含撤回的消息", defaultValue = "false")
    private Boolean includeRevoked = false;

    @Schema(description = "是否精确匹配", defaultValue = "false")
    private Boolean exactMatch = false;
}
