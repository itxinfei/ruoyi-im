package com.ruoyi.im.vo.config;

import lombok.Data;

import java.util.Map;

/**
 * 隐私设置VO
 */
@Data
public class PrivacySettingsVO {
    /** 显示在线状态 */
    private Boolean showOnlineStatus;
    /** 允许陌生人消息 */
    private Boolean allowStrangerMessage;
    /** 允许语音通话 */
    private Boolean allowVoiceCall;
    /** 允许视频通话 */
    private Boolean allowVideoCall;
    /** 允许被搜索 */
    private Boolean allowSearch;
    /** 扩展字段 */
    private Map<String, Object> extra;

    public static PrivacySettingsVO fromMap(Map<String, Object> map) {
        PrivacySettingsVO vo = new PrivacySettingsVO();
        if (map == null) return vo;
        vo.showOnlineStatus = (Boolean) map.get("showOnlineStatus");
        vo.allowStrangerMessage = (Boolean) map.get("allowStrangerMessage");
        vo.allowVoiceCall = (Boolean) map.get("allowVoiceCall");
        vo.allowVideoCall = (Boolean) map.get("allowVideoCall");
        vo.allowSearch = (Boolean) map.get("allowSearch");
        vo.extra = map;
        return vo;
    }
}
