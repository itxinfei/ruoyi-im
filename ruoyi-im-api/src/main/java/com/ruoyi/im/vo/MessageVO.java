package com.ruoyi.im.vo;

import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.domain.ImUser;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 娑堟伅瑙嗗浘瀵硅薄
 * 鍖呭惈娑堟伅璇︽儏鍜屽彂閫佽€呬俊鎭紝鐢ㄤ簬鍓嶇灞曠ず
 *
 * @author ruoyi
 */
public class MessageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 娑堟伅ID
     */
    private Long id;

    /**
     * 浼氳瘽ID
     */
    private Long sessionId;

    /**
     * 鍙戦€佽€匢D
     */
    private Long senderId;

    /**
     * 鍙戦€佽€呮樀绉?
     */
    private String senderName;

    /**
     * 鍙戦€佽€呭ご鍍?
     */
    private String senderAvatar;

    /**
     * 娑堟伅绫诲瀷
     */
    private String type;

    /**
     * 娑堟伅鍐呭
     */
    private Object content;

    /**
     * 娑堟伅鐘舵€?
     */
    private String status;

    /**
     * 鍥炲鐨勬秷鎭疘D
     */
    private Long replyTo;

    /**
     * 琚洖澶嶇殑娑堟伅鍐呭锛堢畝瑕侊級
     */
    private MessageVO replyToMessage;

    /**
     * 鏃堕棿鎴筹紙姣锛?
     */
    private Long timestamp;

    /**
     * ISO鏍煎紡鏃堕棿瀛楃涓?
     */
    private String time;

    /**
     * 瀹㈡埛绔秷鎭疘D锛堢敤浜庡幓閲嶏級
     */
    private String clientMsgId;

    /**
     * 鏄惁宸叉挙鍥?
     */
    private Boolean revoked;

    /**
     * 鎾ゅ洖鏃堕棿
     */
    private Long revokedTime;

    public MessageVO() {
    }

    /**
     * 浠嶪mMessage鍜孖mUser鏋勫缓MessageVO
     */
    public static MessageVO fromMessage(ImMessage message, ImUser sender) {
        if (message == null) {
            return null;
        }

        MessageVO vo = new MessageVO();
        vo.setId(message.getId());
        vo.setSessionId(message.getConversationId());
        vo.setSenderId(message.getSenderId());
        vo.setType(message.getType() != null ? message.getType().toLowerCase() : "text");
        vo.setStatus(message.getStatus() != null ? message.getStatus().toLowerCase() : "sent");
        vo.setReplyTo(message.getReplyToMessageId());

        // 澶勭悊娑堟伅鍐呭
        vo.setContent(parseContent(message.getType(), message.getContent()));

        // 澶勭悊鏃堕棿
        if (message.getCreateTime() != null) {
            vo.setTimestamp(message.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            vo.setTime(message.getCreateTime().toString());
        } else {
            vo.setTimestamp(System.currentTimeMillis());
            vo.setTime(LocalDateTime.now().toString());
        }

        // 澶勭悊鎾ゅ洖鐘舵€?
        vo.setRevoked("REVOKED".equalsIgnoreCase(message.getStatus()));
        if (message.getRevokedTime() != null) {
            vo.setRevokedTime(message.getRevokedTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }

        // 璁剧疆鍙戦€佽€呬俊鎭?
        if (sender != null) {
            vo.setSenderName(sender.getNickname() != null ? sender.getNickname() : sender.getUsername());
            vo.setSenderAvatar(sender.getAvatar());
        } else {
            vo.setSenderName("鏈煡鐢ㄦ埛");
            vo.setSenderAvatar(null);
        }

        return vo;
    }

    /**
     * 瑙ｆ瀽娑堟伅鍐呭
     * 鏂囨湰娑堟伅杩斿洖绾枃鏈紝鍏朵粬绫诲瀷杩斿洖瑙ｆ瀽鍚庣殑瀵硅薄
     */
    private static Object parseContent(String type, String content) {
        if (content == null) {
            return "";
        }

        // 灏濊瘯瑙ｆ瀽JSON鏍煎紡鐨勫唴瀹?
        if (content.startsWith("{") && content.endsWith("}")) {
            try {
                // 濡傛灉鏄枃鏈秷鎭紝鎻愬彇text瀛楁
                if ("TEXT".equalsIgnoreCase(type)) {
                    // 绠€鍗曡В鏋?{"text":"xxx"} 鏍煎紡
                    if (content.contains("\"text\"")) {
                        int start = content.indexOf("\"text\"") + 8;
                        int end = content.lastIndexOf("\"");
                        if (start < end) {
                            return content.substring(start, end);
                        }
                    }
                }
                // 鍏朵粬绫诲瀷杩斿洖鍘熷JSON瀛楃涓诧紝璁╁墠绔В鏋?
                return content;
            } catch (Exception e) {
                return content;
            }
        }

        return content;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderAvatar() {
        return senderAvatar;
    }

    public void setSenderAvatar(String senderAvatar) {
        this.senderAvatar = senderAvatar;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(Long replyTo) {
        this.replyTo = replyTo;
    }

    public MessageVO getReplyToMessage() {
        return replyToMessage;
    }

    public void setReplyToMessage(MessageVO replyToMessage) {
        this.replyToMessage = replyToMessage;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getClientMsgId() {
        return clientMsgId;
    }

    public void setClientMsgId(String clientMsgId) {
        this.clientMsgId = clientMsgId;
    }

    public Boolean getRevoked() {
        return revoked;
    }

    public void setRevoked(Boolean revoked) {
        this.revoked = revoked;
    }

    public Long getRevokedTime() {
        return revokedTime;
    }

    public void setRevokedTime(Long revokedTime) {
        this.revokedTime = revokedTime;
    }
}
