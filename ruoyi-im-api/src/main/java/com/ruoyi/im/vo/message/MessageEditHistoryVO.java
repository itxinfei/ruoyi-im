package com.ruoyi.im.vo.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息编辑历史VO
 */
@Data
@Schema(description = "消息编辑历史")
public class MessageEditHistoryVO implements Serializable {

    @Schema(description = "历史ID")
    private Long id;

    @Schema(description = "消息ID")
    private Long messageId;

    @Schema(description = "编辑前的内容")
    private String oldContent;

    @Schema(description = "编辑后的内容")
    private String newContent;

    @Schema(description = "编辑人ID")
    private Long editorId;

    @Schema(description = "编辑人姓名")
    private String editorName;

    @Schema(description = "编辑人头像")
    private String editorAvatar;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "编辑时间")
    private LocalDateTime editTime;
}
