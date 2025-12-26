package com.ruoyi.im.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.im.service.IImWebSocketService;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WebSocket控制器
 * 
 * @author ruoyi
 * @date 2024-08-09
 */
@Component
@ServerEndpoint("/websocket/{token}")
public class ImWebSocketController
{
    private static final Logger logger = LoggerFactory.getLogger(ImWebSocketController.class);
    
    // 存储WebSocket连接
    private static Map<String, Session> sessionMap = new ConcurrentHashMap<>();
    
    // 存储token与Session的映射
    private static Map<String, Session> tokenSessionMap = new ConcurrentHashMap<>();
    // 存储用户ID与Session的映射
    private static Map<String, String> userSessionMap = new ConcurrentHashMap<>();
    
    private static IImWebSocketService webSocketService;
    
    @Autowired
    public void setWebSocketService(IImWebSocketService webSocketService) {
        ImWebSocketController.webSocketService = webSocketService;
    }

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        try {
            // 验证token并获取用户ID
            Long userId = validateTokenAndGetUserId(token);
            if (userId == null) {
                logger.warn("无效的token: {}", token);
                session.close();
                return;
            }
            
            sessionMap.put(session.getId(), session);
            tokenSessionMap.put(token, session);
            userSessionMap.put(userId.toString(), session.getId());
            
            logger.info("用户{}连接WebSocket成功，sessionId:{}", userId, session.getId());
            
            // 更新用户在线状态
            if (webSocketService != null) {
                webSocketService.updateUserOnlineStatus(userId, "online");
            }
            
            // 发送连接成功消息
            JSONObject message = new JSONObject();
            message.put("action", "connect");
            message.put("status", "success");
            message.put("message", "连接成功");
            message.put("serverMsgId", System.currentTimeMillis());
            message.put("ts", System.currentTimeMillis());
            sendMessage(session, message.toJSONString());
            
        } catch (Exception e) {
            logger.error("WebSocket连接异常", e);
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session, @PathParam("token") String token) {
        try {
            Long userId = validateTokenAndGetUserId(token);
            sessionMap.remove(session.getId());
            tokenSessionMap.remove(token);
            if (userId != null) {
                userSessionMap.remove(userId.toString());
                logger.info("用户{}断开WebSocket连接，sessionId:{}", userId, session.getId());
                
                // 更新用户离线状态
                if (webSocketService != null) {
                    webSocketService.updateUserOnlineStatus(userId, "offline");
                }
            }
            
        } catch (Exception e) {
            logger.error("WebSocket关闭异常", e);
        }
    }

    /**
     * 收到客户端消息后调用的方法
     */
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("token") String token) {
        try {
            Long userId = validateTokenAndGetUserId(token);
            if (userId == null) {
                logger.warn("无效的token: {}", token);
                return;
            }
            
            logger.info("收到用户{}的消息:{}", userId, message);
            
            JSONObject messageObj = JSON.parseObject(message);
            String action = messageObj.getString("action");
            
            switch (action) {
                case "heartbeat":
                    handleHeartbeat(session, userId.toString());
                    break;
                case "message/send":
                    handleMessage(messageObj, userId.toString());
                    break;
                case "message/read":
                    handleReadMessage(messageObj, userId.toString());
                    break;
                case "typing/start":
                case "typing/stop":
                    handleTyping(messageObj, userId.toString());
                    break;
                case "presence/subscribe":
                    handlePresenceSubscribe(messageObj, userId.toString());
                    break;
                default:
                    logger.warn("未知消息动作:{}", action);
                    break;
            }
            
        } catch (Exception e) {
            logger.error("处理WebSocket消息异常", e);
        }
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error, @PathParam("token") String token) {
        logger.error("token {}的WebSocket发生错误", token, error);
    }

    /**
     * 验证token并获取用户ID
     */
    private Long validateTokenAndGetUserId(String token) {
        try {
            // TODO: 实现token验证逻辑，这里暂时返回一个模拟的用户ID
            // 实际应该调用JWT工具类验证token
            if (token != null && !token.isEmpty()) {
                // 这里应该调用JWT验证，暂时返回1作为示例
                return 1L;
            }
            return null;
        } catch (Exception e) {
            logger.error("验证token失败", e);
            return null;
        }
    }

    /**
     * 处理心跳消息
     */
    private void handleHeartbeat(Session session, String userId) {
        try {
            JSONObject response = new JSONObject();
            response.put("type", "heartbeat");
            response.put("timestamp", System.currentTimeMillis());
            sendMessage(session, response.toJSONString());
            
            // 更新用户最后活跃时间
            if (webSocketService != null) {
                webSocketService.updateUserLastActiveTime(Long.valueOf(userId));
            }
        } catch (Exception e) {
            logger.error("处理心跳消息异常", e);
        }
    }

    /**
     * 处理聊天消息
     */
    private void handleMessage(JSONObject messageObj, String userId) {
        try {
            if (webSocketService != null) {
                webSocketService.handleChatMessage(messageObj, Long.valueOf(userId));
            }
        } catch (Exception e) {
            logger.error("处理聊天消息异常", e);
        }
    }

    /**
     * 处理正在输入消息
     */
    private void handleTyping(JSONObject messageObj, String userId) {
        try {
            String conversationId = messageObj.getString("conversationId");
            if (webSocketService != null) {
                webSocketService.handleTypingMessage(Long.valueOf(conversationId), Long.valueOf(userId));
            }
        } catch (Exception e) {
            logger.error("处理正在输入消息异常", e);
        }
    }

    /**
     * 处理消息已读
     */
    private void handleReadMessage(JSONObject messageObj, String userId) {
        try {
            Long messageId = messageObj.getLong("messageId");
            if (webSocketService != null) {
                webSocketService.handleReadMessage(messageId, Long.valueOf(userId));
            }
        } catch (Exception e) {
            logger.error("处理消息已读异常", e);
        }
    }

    /**
     * 处理在线状态订阅
     */
    private void handlePresenceSubscribe(JSONObject messageObj, String userId) {
        try {
            // TODO: 实现在线状态订阅处理
            logger.info("处理用户{}的在线状态订阅", userId);
        } catch (Exception e) {
            logger.error("处理在线状态订阅异常", e);
        }
    }

    /**
     * 发送消息给指定用户
     */
    public static void sendMessageToUser(String userId, String message) {
        try {
            String sessionId = userSessionMap.get(userId);
            if (sessionId != null) {
                Session session = sessionMap.get(sessionId);
                if (session != null && session.isOpen()) {
                    sendMessage(session, message);
                }
            }
        } catch (Exception e) {
            logger.error("发送消息给用户{}异常", userId, e);
        }
    }

    /**
     * 群发消息
     */
    public static void broadcastMessage(String message) {
        sessionMap.values().forEach(session -> {
            if (session.isOpen()) {
                sendMessage(session, message);
            }
        });
    }

    /**
     * 发送消息给会话中的所有用户
     */
    public static void sendMessageToConversation(List<String> userIds, String message) {
        userIds.forEach(userId -> sendMessageToUser(userId, message));
    }

    /**
     * 发送消息
     */
    private static void sendMessage(Session session, String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            logger.error("发送WebSocket消息异常", e);
        }
    }

    /**
     * 获取在线用户数量
     */
    public static int getOnlineUserCount() {
        return userSessionMap.size();
    }

    /**
     * 获取在线用户列表
     */
    public static List<String> getOnlineUsers() {
        return new ArrayList<>(userSessionMap.keySet());
    }

    /**
     * 检查用户是否在线
     */
    public static boolean isUserOnline(String userId) {
        return userSessionMap.containsKey(userId);
    }

    /**
     * 关闭用户连接
     */
    public static void closeUserConnection(String userId) {
        try {
            String sessionId = userSessionMap.get(userId);
            if (sessionId != null) {
                Session session = sessionMap.get(sessionId);
                if (session != null && session.isOpen()) {
                    session.close();
                }
            }
        } catch (Exception e) {
            logger.error("关闭用户{}连接异常", userId, e);
        }
    }
}

/**
 * WebSocket REST API控制器
 */
@RestController
@RequestMapping("/im/websocket")
class ImWebSocketRestController extends BaseController {
    
    /**
     * 获取在线用户数量
     */
    @GetMapping("/onlineCount")
    public AjaxResult getOnlineUserCount() {
        int count = ImWebSocketController.getOnlineUserCount();
        return success(count);
    }
    
    /**
     * 获取在线用户列表
     */
    @GetMapping("/onlineUsers")
    public AjaxResult getOnlineUsers() {
        List<String> users = ImWebSocketController.getOnlineUsers();
        return success(users);
    }
    
    /**
     * 检查用户是否在线
     */
    @GetMapping("/isOnline/{userId}")
    public AjaxResult isUserOnline(@PathVariable String userId) {
        boolean isOnline = ImWebSocketController.isUserOnline(userId);
        return success(isOnline);
    }
    
    /**
     * 发送消息给指定用户
     */
    @GetMapping("/sendMessage/{userId}/{message}")
    public AjaxResult sendMessageToUser(@PathVariable String userId, @PathVariable String message) {
        ImWebSocketController.sendMessageToUser(userId, message);
        return success("消息发送成功");
    }
    
    /**
     * 关闭用户连接
     */
    @GetMapping("/closeConnection/{userId}")
    public AjaxResult closeUserConnection(@PathVariable String userId) {
        ImWebSocketController.closeUserConnection(userId);
        return success("连接关闭成功");
    }
}