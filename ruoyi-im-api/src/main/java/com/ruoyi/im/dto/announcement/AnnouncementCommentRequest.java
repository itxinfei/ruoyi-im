package com.ruoyi.im.dto.announcement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 公告评论请求
 *
 * @author ruoyi
 */
@Data
@Schema(description = "公告评论请求")
public class AnnouncementCommentRequest {

    @NotBlank(message = "评论内容不能为空")
    @Schema(description = "评论内容", required = true, example = "这是一条评论")
    private String content;
}