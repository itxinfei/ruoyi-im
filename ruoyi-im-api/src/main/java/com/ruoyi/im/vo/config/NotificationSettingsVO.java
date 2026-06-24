package com.ruoyi.im.vo.config;

import lombok.Data;

import java.util.Map;

/**
 * 通知设置VO
 */
@Data
public class NotificationSettingsVO {
    /** 消息通知开关 */
    private Boolean messageNotification;
    /** 群组通知开关 */
    private Boolean groupNotification;
    /** 系统通知开关 */
    private Boolean systemNotification;
    /** 声音提醒 */
    private Boolean soundEnabled;
    /** 桌面通知 */
    private Boolean desktopNotification;
    /** 免打扰时段开始 */
    private String quietHoursStart;
    /** 免打扰时段结束 */
    private String quietHoursEnd;
    /** 扩展字段 */
    private Map<String, Object> extra;

    public static NotificationSettingsVO fromMap(Map<String, Object> map) {
        NotificationSettingsVO vo = new NotificationSettingsVO();
        if (map == null) return vo;
        vo.messageNotification = (Boolean) map.get("messageNotification");
        vo.groupNotification = (Boolean) map.get("groupNotification");
        vo.systemNotification = (Boolean) map.get("systemNotification");
        vo.soundEnabled = (Boolean) map.get("soundEnabled");
        vo.desktopNotification = (Boolean) map.get("desktopNotification");
        vo.quietHoursStart = (String) map.get("quietHoursStart");
        vo.quietHoursEnd = (String) map.get("quietHoursEnd");
        vo.extra = map;
        return vo;
    }
}
