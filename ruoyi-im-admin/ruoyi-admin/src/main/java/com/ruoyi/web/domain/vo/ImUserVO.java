package com.ruoyi.web.domain.vo;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * IM用户信息VO
 * 
 * @author ruoyi
 * @version 1.0
 * @since 2025-01-22
 */
@Data
public class ImUserVO {

    private Long id;

    private String username;

    private String nickname;

    private String email;

    private String mobile;

    private String avatar;

    private Integer gender;

    private String genderDisplay;

    private String signature;

    private Integer status;

    private String statusDisplay;

    private Integer isOnline;

    private String isOnlineDisplay;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastOnlineTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    private Long deptId;

    private String deptName;

    private String[] roleNames;

    public String getGenderDisplay() {
        if (gender == null) return "未知";
        switch (gender) {
            case 0: return "男";
            case 1: return "女";
            default: return "未知";
        }
    }

    public String getStatusDisplay() {
        if (status == null) return "未知";
        return status == 0 ? "正常" : "禁用";
    }

    public String getIsOnlineDisplay() {
        if (isOnline == null) return "离线";
        return isOnline == 1 ? "在线" : "离线";
    }
}