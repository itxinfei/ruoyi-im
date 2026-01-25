package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考勤组成员实体
 *
 * @author ruoyi
 */
@TableName("im_attendance_group_member")
@Data
@Schema(description = "考勤组成员")
public class ImAttendanceGroupMember implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 成员ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "成员ID")
    private Long id;

    /**
     * 考勤组ID
     */
    @Schema(description = "考勤组ID")
    @TableField("group_id")
    private Long groupId;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    @TableField("user_id")
    private Long userId;

    /**
     * 用户名称（冗余）
     */
    @Schema(description = "用户名称")
    @TableField("user_name")
    private String userName;

    /**
     * 成员角色：ADMIN管理员, MEMBER普通成员
     */
    @Schema(description = "成员角色")
    @TableField("role")
    private String role;

    /**
     * 入组时间
     */
    @Schema(description = "入组时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("join_time")
    private LocalDateTime joinTime;

    /**
     * 离组时间
     */
    @Schema(description = "离组时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("leave_time")
    private LocalDateTime leaveTime;

    /**
     * 状态：ACTIVE在职, LEFT已离组
     */
    @Schema(description = "状态")
    @TableField("status")
    private String status;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;
}
