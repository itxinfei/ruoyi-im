package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息编辑历史实体类
 */
@TableName("im_message_edit_history")
@Data
@Schema(description = "消息编辑历史")
public class ImMessageEditHistory implements Serializable {

    @Schema(description = "历史ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("message_id")
    @Schema(description = "消息ID")
    private Long messageId;

    @Schema(description = "编辑前的内容")
    private String oldContent;

    @Schema(description = "编辑后的内容")
    private String newContent;

    @TableField("editor_id")
    @Schema(description = "编辑人ID")
    private Long editorId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "编辑时间")
    private LocalDateTime editTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(exist = false)
    @Schema(description = "编辑人姓名")
    private String editorName;

    @TableField(exist = false)
    @Schema(description = "编辑人头像")
    private String editorAvatar;
}
