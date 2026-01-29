package com.ruoyi.im.vo.nudge;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 拍一拍响应 VO
 *
 * @author ruoyi
 */
@Schema(description = "拍一拍响应")
@Data
public class ImNudgeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "拍一拍记录ID")
    private Long id;

    @Schema(description = "会话ID")
    private Long conversationId;

    @Schema(description = "发起者ID")
    private Long nudgerId;

    @Schema(description = "发起者昵称")
    private String nudgerName;

    @Schema(description = "发起者头像")
    private String nudgerAvatar;

    @Schema(description = "被拍用户ID")
    private Long nudgedUserId;

    @Schema(description = "被拍用户昵称")
    private String nudgedUserName;

    @Schema(description = "被拍用户头像")
    private String nudgedUserAvatar;

    @Schema(description = "连续拍拍次数")
    private Integer nudgeCount;

    @Schema(description = "提示文本")
    private String hint;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /** 构造方法 */
    public ImNudgeVO() {
    }

    /** 设置提示文本 */
    public void setNudgeCount(Integer nudgeCount) {
        this.nudgeCount = nudgeCount;
        generateHint();
    }

    /** 生成提示文本 */
    private void generateHint() {
        if (nudgeCount == null || nudgeCount == 1) {
            this.hint = "拍了拍";
        } else if (nudgeCount == 2) {
            this.hint = "拍了拍两下";
        } else if (nudgeCount == 3) {
            this.hint = "拍了拍三下";
        } else if (nudgeCount <= 5) {
            this.hint = "疯狂拍了拍";
        } else {
            this.hint = "把" + (nudgedUserName != null ? nudgedUserName : "对方") + "拍了冒烟了";
        }
    }
}
