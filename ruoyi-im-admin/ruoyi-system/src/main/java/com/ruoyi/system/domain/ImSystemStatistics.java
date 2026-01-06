package com.ruoyi.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统统计实体
 *
 * @author ruoyi
 */
@TableName("im_system_statistics")
@Data
@Schema(description = "系统统计数据")
public class ImSystemStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "统计ID")
    private Long id;

    @Schema(description = "统计日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private String statisticsDate;

    @Schema(description = "总用户数")
    private Integer totalUsers;

    @Schema(description = "活跃用户数")
    private Integer activeUsers;

    @Schema(description = "新注册用户数")
    private Integer newUsers;

    @Schema(description = "今日登录用户数")
    private Integer todayLoginUsers;

    @Schema(description = "今日消息数")
    private Integer todayMessages;

    @Schema(description = "今日群组消息数")
    private Integer todayGroupMessages;

    @Schema(description = "总消息数")
    private Integer totalMessages;

    @Schema(description = "总文件数")
    private Integer totalFiles;

    @Schema(description = "总存储大小（MB）")
    private Long totalStorageSize;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}