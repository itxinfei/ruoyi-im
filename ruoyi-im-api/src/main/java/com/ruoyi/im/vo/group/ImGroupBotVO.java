package com.ruoyi.im.vo.group;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 群机器人视图对象
 * 用于返回给前端的群机器人数据，与数据库实体 ImGroupBot 分离
 *
 * @author ruoyi
 */
@Data
public class ImGroupBotVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 机器人ID */
    private Long id;

    /** 群组ID */
    private Long groupId;

    /** 群组名称（关联查询） */
    private String groupName;

    /** 机器人名称 */
    private String botName;

    /** 机器人类型：SERVICE客服/NOTIFY通知/MANAGE管理 */
    private String botType;

    /** 机器人头像 */
    private String avatar;

    /** 机器人描述 */
    private String description;

    /** 是否启用：0=禁用, 1=启用 */
    private Integer isEnabled;

    /** 机器人规则列表 */
    private List<ImGroupBotRuleVO> rules;

    /** 规则数量 */
    private Integer ruleCount;

    /** 创建者 */
    private String createBy;

    /** 更新者 */
    private String updateBy;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 群机器人规则视图对象
     */
    @Data
    public static class ImGroupBotRuleVO implements Serializable {

        private static final long serialVersionUID = 1L;

        /** 规则ID */
        private Long id;

        /** 机器人ID */
        private Long botId;

        /** 规则名称 */
        private String ruleName;

        /** 触发关键词 */
        private String keywords;

        /** 回复内容 */
        private String replyContent;

        /** 是否启用 */
        private Integer isEnabled;

        /** 创建时间 */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;
    }
}
