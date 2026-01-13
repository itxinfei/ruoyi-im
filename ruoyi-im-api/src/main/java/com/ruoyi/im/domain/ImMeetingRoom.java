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
 * 会议室实体类
 *
 * @author ruoyi
 */
@TableName("im_meeting_room")
@Data
@Schema(description = "会议室")
public class ImMeetingRoom implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "会议室ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("room_name")
    @Schema(description = "会议室名称")
    private String roomName;

    @TableField("room_number")
    @Schema(description = "会议室编号")
    private String roomNumber;

    @TableField("department_id")
    @Schema(description = "所属部门ID")
    private Long departmentId;

    @TableField("location")
    @Schema(description = "位置")
    private String location;

    @TableField("floor")
    @Schema(description ="楼层")
    private Integer floor;

    @TableField("capacity")
    @Schema(description = "容纳人数")
    private Integer capacity;

    @TableField("has_projector")
    @Schema(description = "是否有投影仪")
    private Boolean hasProjector;

    @TableField("has_whiteboard")
    @Schema(description = "是否有白板")
    private Boolean hasWhiteboard;

    @TableField("has_video_conf")
    @Schema(description = "是否支持视频会议")
    private Boolean hasVideoConf;

    @TableField("has_phone")
    @Schema(description = "是否有电话")
    private Boolean hasPhone;

    @TableField("facilities")
    @Schema(description = "设施列表（JSON格式）")
    private String facilities;

    @TableField("photos")
    @Schema(description = "会议室图片（JSON格式）")
    private String photos;

    @TableField("status")
    @Schema(description = "状态（AVAILABLE可用 MAINTENANCE维护中 DISABLED停用）")
    private String status;

    @TableField("is_bookable")
    @Schema(description = "是否可预订")
    private Boolean isBookable;

    @TableField("description")
    @Schema(description = "描述")
    private String description;

    @TableField("remark")
    @Schema(description = "备注")
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    @TableField("update_time")
    private LocalDateTime updateTime;

    // ==================== 非数据库字段 ====================

    @TableField(exist = false)
    @Schema(description = "部门名称")
    private String departmentName;

    @TableField(exist = false)
    @Schema(description = "是否被占用")
    private Boolean isOccupied;

    @TableField(exist = false)
    @Schema(description = "当前预订数")
    private Integer bookingCount;
}
