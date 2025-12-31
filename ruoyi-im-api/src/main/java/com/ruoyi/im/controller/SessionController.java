package com.ruoyi.im.controller;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * IM会话控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/im/session")
public class SessionController {

    /**
     * 获取会话列表
     */
    @GetMapping("/list")
    public Map<String, Object> listSessions(@RequestParam(defaultValue = "1") Integer pageNum,
                                           @RequestParam(defaultValue = "20") Integer pageSize,
                                           @RequestParam(required = false) String type) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取会话列表（简化实现）
            List<Map<String, Object>> sessions = getSessionsByType(type);
            
            int start = (pageNum - 1) * pageSize;
            int end = Math.min(start + pageSize, sessions.size());
            
            List<Map<String, Object>> pagedSessions = start < sessions.size() ? 
                sessions.subList(start, end) : List.of();
            
            Map<String, Object> pageResult = new HashMap<>();
            pageResult.put("rows", pagedSessions);
            pageResult.put("total", sessions.size());
            pageResult.put("pageNum", pageNum);
            pageResult.put("pageSize", pageSize);
            
            result.put("code", 200);
            result.put("msg", "查询成功");
            result.put("data", pageResult);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "查询失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 创建会话
     */
    @PostMapping
    public Map<String, Object> createSession(@RequestBody Map<String, Object> sessionData) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取会话参数
            String type = sessionData.get("type").toString();
            String name = sessionData.get("name").toString();
            List<Long> participantIds = (List<Long>) sessionData.get("participantIds");
            
            // 创建会话对象（简化实现）
            Map<String, Object> session = new HashMap<>();
            session.put("id", System.currentTimeMillis());
            session.put("type", type);
            session.put("name", name);
            session.put("participantIds", participantIds);
            session.put("createTime", LocalDateTime.now());
            session.put("updateTime", LocalDateTime.now());
            session.put("lastMessage", null);
            session.put("unreadCount", 0);
            
            // 保存会话（简化实现）
            saveSessionToMemory(session);
            
            result.put("code", 200);
            result.put("msg", "会话创建成功");
            result.put("data", session);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "会话创建失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取会话详情
     */
    @GetMapping("/{sessionId}")
    public Map<String, Object> getSession(@PathVariable String sessionId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, Object> session = getSessionById(sessionId);
            if (session != null) {
                result.put("code", 200);
                result.put("msg", "查询成功");
                result.put("data", session);
            } else {
                result.put("code", 404);
                result.put("msg", "会话不存在");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "查询失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 更新会话
     */
    @PutMapping
    public Map<String, Object> updateSession(@RequestBody Map<String, Object> sessionData) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String sessionId = sessionData.get("id").toString();
            Map<String, Object> session = getSessionById(sessionId);
            
            if (session != null) {
                // 更新会话信息
                if (sessionData.containsKey("name")) {
                    session.put("name", sessionData.get("name"));
                }
                if (sessionData.containsKey("mute")) {
                    session.put("mute", sessionData.get("mute"));
                }
                
                session.put("updateTime", LocalDateTime.now());
                
                // 更新会话（简化实现）
                updateSessionInMemory(session);
                
                result.put("code", 200);
                result.put("msg", "会话更新成功");
                result.put("data", session);
            } else {
                result.put("code", 404);
                result.put("msg", "会话不存在");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "更新失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/{sessionId}")
    public Map<String, Object> deleteSession(@PathVariable String sessionId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean deleted = deleteSessionFromMemory(sessionId);
            if (deleted) {
                result.put("code", 200);
                result.put("msg", "会话删除成功");
            } else {
                result.put("code", 404);
                result.put("msg", "会话不存在");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "删除失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取会话成员
     */
    @GetMapping("/{sessionId}/members")
    public Map<String, Object> getSessionMembers(@PathVariable String sessionId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取会话成员列表（简化实现）
            List<Map<String, Object>> members = getSessionMembersById(sessionId);
            
            result.put("code", 200);
            result.put("msg", "查询成功");
            result.put("data", members);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "查询失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 添加会话成员
     */
    @PostMapping("/{sessionId}/members")
    public Map<String, Object> addSessionMembers(@PathVariable String sessionId, @RequestBody List<Long> userIds) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 添加成员到会话（简化实现）
            Map<String, Object> session = getSessionById(sessionId);
            if (session != null) {
                @SuppressWarnings("unchecked")
                List<Long> existingMembers = (List<Long>) session.get("participantIds");
                if (existingMembers == null) {
                    existingMembers = List.of();
                }
                
                List<Long> newMembers = userIds.stream()
                    .filter(id -> !existingMembers.contains(id))
                    .collect(Collectors.toList());
                
                List<Long> allMembers = new java.util.ArrayList<>(existingMembers);
                allMembers.addAll(newMembers);
                
                session.put("participantIds", allMembers);
                session.put("updateTime", LocalDateTime.now());
                
                updateSessionInMemory(session);
                
                result.put("code", 200);
                result.put("msg", "成员添加成功");
                result.put("data", newMembers);
            } else {
                result.put("code", 404);
                result.put("msg", "会话不存在");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "添加成员失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 移除会话成员
     */
    @DeleteMapping("/{sessionId}/members/{userId}")
    public Map<String, Object> removeSessionMember(@PathVariable String sessionId, @PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 从会话中移除成员（简化实现）
            Map<String, Object> session = getSessionById(sessionId);
            if (session != null) {
                @SuppressWarnings("unchecked")
                List<Long> existingMembers = (List<Long>) session.get("participantIds");
                
                if (existingMembers != null && existingMembers.contains(userId)) {
                    List<Long> updatedMembers = existingMembers.stream()
                        .filter(id -> !id.equals(userId))
                        .collect(Collectors.toList());
                    
                    session.put("participantIds", updatedMembers);
                    session.put("updateTime", LocalDateTime.now());
                    
                    updateSessionInMemory(session);
                    
                    result.put("code", 200);
                    result.put("msg", "成员移除成功");
                } else {
                    result.put("code", 404);
                    result.put("msg", "成员不存在于会话中");
                }
            } else {
                result.put("code", 404);
                result.put("msg", "会话不存在");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "移除成员失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 设置会话置顶
     */
    @PutMapping("/{sessionId}/top")
    public Map<String, Object> setSessionTop(@PathVariable String sessionId, @RequestParam Boolean top) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, Object> session = getSessionById(sessionId);
            if (session != null) {
                session.put("isTop", top);
                session.put("updateTime", LocalDateTime.now());
                
                updateSessionInMemory(session);
                
                result.put("code", 200);
                result.put("msg", "置顶状态更新成功");
            } else {
                result.put("code", 404);
                result.put("msg", "会话不存在");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "更新置顶状态失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 设置会话免打扰
     */
    @PutMapping("/{sessionId}/mute")
    public Map<String, Object> setSessionMute(@PathVariable String sessionId, @RequestParam Boolean mute) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, Object> session = getSessionById(sessionId);
            if (session != null) {
                session.put("mute", mute);
                session.put("updateTime", LocalDateTime.now());
                
                updateSessionInMemory(session);
                
                result.put("code", 200);
                result.put("msg", "免打扰状态更新成功");
            } else {
                result.put("code", 404);
                result.put("msg", "会话不存在");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "更新免打扰状态失败: " + e.getMessage());
        }
        
        return result;
    }

    // 以下方法是简化实现，实际项目中应使用数据库
    private List<Map<String, Object>> getSessionsByType(String type) {
        // 根据类型获取会话列表
        // 实际项目中应从数据库查询
        return List.of();
    }
    
    private Map<String, Object> getSessionById(String sessionId) {
        // 根据ID获取会话
        // 实际项目中应从数据库查询
        return null;
    }
    
    private void saveSessionToMemory(Map<String, Object> session) {
        // 保存会话到内存
        // 实际项目中应持久化到数据库
    }
    
    private void updateSessionInMemory(Map<String, Object> session) {
        // 更新内存中的会话
        // 实际项目中应更新数据库
    }
    
    private boolean deleteSessionFromMemory(String sessionId) {
        // 从内存删除会话
        // 实际项目中应从数据库删除
        return true;
    }
    
    private List<Map<String, Object>> getSessionMembersById(String sessionId) {
        // 获取会话成员列表
        // 实际项目中应从数据库查询
        return List.of();
    }
}