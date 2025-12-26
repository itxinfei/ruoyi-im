package com.ruoyi.im.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * IM用户对象 im_user
 * 
 * @author ruoyi
 * @date 2024-08-09
 */
public class ImUser extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long id;

    /** 用户名 */
    @Excel(name = "用户名")
    private String username;

    /** 昵称 */
    @Excel(name = "昵称")
    private String nickname;

    /** 邮箱 */
    @Excel(name = "邮箱")
    private String email;

    /** 手机号 */
    @Excel(name = "手机号")
    private String phone;

    /** 头像URL */
    @Excel(name = "头像URL")
    private String avatar;

    /** 性别（0男 1女 2未知） */
    @Excel(name = "性别", readConverterExp = "0=男,1=女,2=未知")
    private String gender;

    /** 生日 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "生日", width = 30, dateFormat = "yyyy-MM-dd")
    private Date birthday;

    /** 个人签名 */
    @Excel(name = "个人签名")
    private String signature;

    /** 在线状态（0离线 1在线 2忙碌 3离开） */
    @Excel(name = "在线状态")
    private String onlineStatus;

    /** 最后活跃时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最后活跃时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date lastActiveTime;

    /** 注册时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "注册时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date registerTime;

    /** 用户状态（0正常 1停用） */
    @Excel(name = "用户状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    /** 用户设置 */
    private String settings;

    /** 地区 */
    @Excel(name = "地区")
    private String region;

    /** 语言 */
    @Excel(name = "语言")
    private String language;

    /** 时区 */
    @Excel(name = "时区")
    private String timezone;

    /** 是否允许添加好友（0允许 1不允许） */
    @Excel(name = "是否允许添加好友", readConverterExp = "0=允许,1=不允许")
    private String allowAddFriend;

    /** 是否允许群组邀请（0允许 1不允许） */
    @Excel(name = "是否允许群组邀请", readConverterExp = "0=允许,1=不允许")
    private String allowGroupInvite;

    /** 消息通知设置（0全部通知 1仅好友 2关闭通知） */
    @Excel(name = "消息通知设置", readConverterExp = "0=全部通知,1=仅好友,2=关闭通知")
    private String notificationSetting;

    /** 隐私设置 */
    private String privacySettings;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }
    public void setUsername(String username) 
    {
        this.username = username;
    }

    public String getUsername() 
    {
        return username;
    }
    public void setNickname(String nickname) 
    {
        this.nickname = nickname;
    }

    public String getNickname() 
    {
        return nickname;
    }
    public void setEmail(String email) 
    {
        this.email = email;
    }

    public String getEmail() 
    {
        return email;
    }
    public void setPhone(String phone) 
    {
        this.phone = phone;
    }

    public String getPhone() 
    {
        return phone;
    }
    public void setAvatar(String avatar) 
    {
        this.avatar = avatar;
    }

    public String getAvatar() 
    {
        return avatar;
    }
    public void setGender(String gender) 
    {
        this.gender = gender;
    }

    public String getGender() 
    {
        return gender;
    }
    public void setBirthday(Date birthday) 
    {
        this.birthday = birthday;
    }

    public Date getBirthday() 
    {
        return birthday;
    }
    public void setSignature(String signature) 
    {
        this.signature = signature;
    }

    public String getSignature() 
    {
        return signature;
    }
    public void setOnlineStatus(String onlineStatus) 
    {
        this.onlineStatus = onlineStatus;
    }

    public String getOnlineStatus() 
    {
        return onlineStatus;
    }
    public void setLastActiveTime(Date lastActiveTime) 
    {
        this.lastActiveTime = lastActiveTime;
    }

    public Date getLastActiveTime() 
    {
        return lastActiveTime;
    }
    public void setRegisterTime(Date registerTime) 
    {
        this.registerTime = registerTime;
    }

    public Date getRegisterTime() 
    {
        return registerTime;
    }
    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }
    public void setDelFlag(String delFlag) 
    {
        this.delFlag = delFlag;
    }

    public String getDelFlag() 
    {
        return delFlag;
    }
    public void setSettings(String settings) 
    {
        this.settings = settings;
    }

    public String getSettings() 
    {
        return settings;
    }
    public void setRegion(String region) 
    {
        this.region = region;
    }

    public String getRegion() 
    {
        return region;
    }
    public void setLanguage(String language) 
    {
        this.language = language;
    }

    public String getLanguage() 
    {
        return language;
    }
    public void setTimezone(String timezone) 
    {
        this.timezone = timezone;
    }

    public String getTimezone() 
    {
        return timezone;
    }
    public void setAllowAddFriend(String allowAddFriend) 
    {
        this.allowAddFriend = allowAddFriend;
    }

    public String getAllowAddFriend() 
    {
        return allowAddFriend;
    }
    public void setAllowGroupInvite(String allowGroupInvite) 
    {
        this.allowGroupInvite = allowGroupInvite;
    }

    public String getAllowGroupInvite() 
    {
        return allowGroupInvite;
    }
    public void setNotificationSetting(String notificationSetting) 
    {
        this.notificationSetting = notificationSetting;
    }

    public String getNotificationSetting() 
    {
        return notificationSetting;
    }
    public void setPrivacySettings(String privacySettings) 
    {
        this.privacySettings = privacySettings;
    }

    public String getPrivacySettings() 
    {
        return privacySettings;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("username", getUsername())
            .append("nickname", getNickname())
            .append("email", getEmail())
            .append("phone", getPhone())
            .append("avatar", getAvatar())
            .append("gender", getGender())
            .append("birthday", getBirthday())
            .append("signature", getSignature())
            .append("onlineStatus", getOnlineStatus())
            .append("lastActiveTime", getLastActiveTime())
            .append("registerTime", getRegisterTime())
            .append("status", getStatus())
            .append("delFlag", getDelFlag())
            .append("settings", getSettings())
            .append("region", getRegion())
            .append("language", getLanguage())
            .append("timezone", getTimezone())
            .append("allowAddFriend", getAllowAddFriend())
            .append("allowGroupInvite", getAllowGroupInvite())
            .append("notificationSetting", getNotificationSetting())
            .append("privacySettings", getPrivacySettings())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}