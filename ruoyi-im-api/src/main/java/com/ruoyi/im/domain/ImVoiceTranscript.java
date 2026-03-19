package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 语音消息转文字记录实体
 *
 * @author ruoyi
 */
@TableName("im_voice_transcript")
@Data

public class ImVoiceTranscript implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    @TableId(type = IdType.AUTO)
    
    private Long id;

    /**
     * 消息ID
     */
    
    @TableField("message_id")
    private Long messageId;

    /**
     * 原始语音文件URL
     */
    
    @TableField("voice_url")
    private String voiceUrl;

    /**
     * 语音时长（秒）
     */
    
    @TableField("duration")
    private Integer duration;

    /**
     * 转换状态：PENDING待处理, PROCESSING处理中, SUCCESS成功, FAILED失败
     */
    
    @TableField("status")
    private String status;

    /**
     * 识别文本结果
     */
    
    @TableField("transcript_text")
    private String transcriptText;

    /**
     * 语言类型：zh-CN中文, en-US英文等
     */
    
    @TableField("language")
    private String language;

    /**
     * 置信度（0-100）
     */
    
    @TableField("confidence")
    private Integer confidence;

    /**
     * 第三方服务商：ALIYUN阿里云, XUNFEI讯飞, TENCENT腾讯
     */
    
    @TableField("provider")
    private String provider;

    /**
     * 服务请求ID
     */
    
    @TableField("request_id")
    private String requestId;

    /**
     * 错误码
     */
    
    @TableField("error_code")
    private String errorCode;

    /**
     * 错误信息
     */
    
    @TableField("error_msg")
    private String errorMsg;

    /**
     * 处理时长（毫秒）
     */
    
    @TableField("process_duration")
    private Long processDuration;

    /**
     * 创建时间
     */
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 完成时间
     */
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("complete_time")
    private LocalDateTime completeTime;
}

