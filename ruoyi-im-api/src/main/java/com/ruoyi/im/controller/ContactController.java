package com.ruoyi.im.controller;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * IM联系人控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/im/contact")
public class ContactController {

    /**
     * 获取联系人列表
     */
    @GetMapping("/list")
    public Map<String, Object> listContacts(@RequestParam(required = false) String keyword,
                                           @RequestParam(defaultValue = "1") Integer pageNum,
                                           @RequestParam(defaultValue = "20") Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取联系人列表（简化实现）
            List<Map<String, Object>> allContacts = getAllContacts();
            
            // 过滤条件
            List<Map<String, Object>> filteredContacts = allContacts.stream()
                .filter((Map<String, Object> contact) -> keyword == null || 
                    contact.get("username").toString().contains(keyword) ||
                    contact.get("nickname").toString().contains(keyword))
                .collect(Collectors.toList());
            
            int start = (pageNum - 1) * pageSize;
            int end = Math.min(start + pageSize, filteredContacts.size());
            
            List<Map<String, Object>> pagedContacts = start < filteredContacts.size() ? 
                filteredContacts.subList(start, end) : java.util.Collections.emptyList();
            
            Map<String, Object> pageResult = new HashMap<>();
            pageResult.put("rows", pagedContacts);
            pageResult.put("total", filteredContacts.size());
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
     * 获取联系人详情
     */
    @GetMapping("/{contactId}")
    public Map<String, Object> getContact(@PathVariable Long contactId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, Object> contact = getContactById(contactId);
            if (contact != null) {
                result.put("code", 200);
                result.put("msg", "查询成功");
                result.put("data", contact);
            } else {
                result.put("code", 404);
                result.put("msg", "联系人不存在");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "查询失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 添加联系人
     */
    @PostMapping
    public Map<String, Object> addContact(@RequestBody Map<String, Object> contactData) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Long userId = Long.valueOf(contactData.get("userId").toString());
            String remark = contactData.get("remark").toString();
            
            // 检查联系人是否已存在（简化实现）
            if (isContactExists(userId)) {
                result.put("code", 400);
                result.put("msg", "联系人已存在");
                return result;
            }
            
            // 创建联系人对象（简化实现）
            Map<String, Object> contact = new HashMap<>();
            contact.put("id", System.currentTimeMillis());
            contact.put("userId", userId);
            contact.put("remark", remark);
            contact.put("createTime", LocalDateTime.now());
            contact.put("updateTime", LocalDateTime.now());
            contact.put("status", 1); // 1表示已添加
            
            // 保存联系人（简化实现）
            saveContactToMemory(contact);
            
            result.put("code", 200);
            result.put("msg", "联系人添加成功");
            result.put("data", contact);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "添加联系人失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 更新联系人
     */
    @PutMapping
    public Map<String, Object> updateContact(@RequestBody Map<String, Object> contactData) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Long contactId = Long.valueOf(contactData.get("id").toString());
            Map<String, Object> contact = getContactById(contactId);
            
            if (contact != null) {
                // 更新联系人信息
                if (contactData.containsKey("remark")) {
                    contact.put("remark", contactData.get("remark"));
                }
                
                contact.put("updateTime", LocalDateTime.now());
                
                // 更新联系人（简化实现）
                updateContactInMemory(contact);
                
                result.put("code", 200);
                result.put("msg", "联系人更新成功");
                result.put("data", contact);
            } else {
                result.put("code", 404);
                result.put("msg", "联系人不存在");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "更新联系人失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 删除联系人
     */
    @DeleteMapping("/{contactId}")
    public Map<String, Object> deleteContact(@PathVariable Long contactId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean deleted = deleteContactFromMemory(contactId);
            if (deleted) {
                result.put("code", 200);
                result.put("msg", "联系人删除成功");
            } else {
                result.put("code", 404);
                result.put("msg", "联系人不存在");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "删除联系人失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 搜索联系人
     */
    @GetMapping("/search")
    public Map<String, Object> searchContacts(@RequestParam String keyword,
                                             @RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "20") Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 搜索联系人（简化实现）
            List<Map<String, Object>> contacts = searchContactsByKeyword(keyword);
            
            int start = (pageNum - 1) * pageSize;
            int end = Math.min(start + pageSize, contacts.size());
            
            List<Map<String, Object>> pagedContacts = start < contacts.size() ? 
                contacts.subList(start, end) : java.util.Collections.emptyList();
            
            Map<String, Object> pageResult = new HashMap<>();
            pageResult.put("rows", pagedContacts);
            pageResult.put("total", contacts.size());
            pageResult.put("pageNum", pageNum);
            pageResult.put("pageSize", pageSize);
            
            result.put("code", 200);
            result.put("msg", "搜索成功");
            result.put("data", pageResult);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "搜索失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取分组联系人
     */
    @GetMapping("/groups")
    public Map<String, Object> getContactGroups() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取联系人分组（简化实现）
            List<Map<String, Object>> contactGroups = getContactGroupsFromMemory();
            
            result.put("code", 200);
            result.put("msg", "查询成功");
            result.put("data", contactGroups);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "查询失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 添加到分组
     */
    @PutMapping("/{contactId}/group")
    public Map<String, Object> addToGroup(@PathVariable Long contactId, @RequestParam Long groupId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, Object> contact = getContactById(contactId);
            if (contact != null) {
                contact.put("groupId", groupId);
                contact.put("updateTime", LocalDateTime.now());
                
                updateContactInMemory(contact);
                
                result.put("code", 200);
                result.put("msg", "添加到分组成功");
            } else {
                result.put("code", 404);
                result.put("msg", "联系人不存在");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "添加到分组失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 设置备注
     */
    @PutMapping("/{contactId}/remark")
    public Map<String, Object> setRemark(@PathVariable Long contactId, @RequestParam String remark) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, Object> contact = getContactById(contactId);
            if (contact != null) {
                contact.put("remark", remark);
                contact.put("updateTime", LocalDateTime.now());
                
                updateContactInMemory(contact);
                
                result.put("code", 200);
                result.put("msg", "备注设置成功");
            } else {
                result.put("code", 404);
                result.put("msg", "联系人不存在");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "设置备注失败: " + e.getMessage());
        }
        
        return result;
    }

    // 以下方法是简化实现，实际项目中应使用数据库
    private List<Map<String, Object>> getAllContacts() {
        // 获取所有联系人
        // 实际项目中应从数据库查询
        return java.util.Collections.emptyList();
    }
    
    private Map<String, Object> getContactById(Long contactId) {
        // 根据ID获取联系人
        // 实际项目中应从数据库查询
        return null;
    }
    
    private boolean isContactExists(Long userId) {
        // 检查联系人是否已存在
        // 实际项目中应从数据库查询
        return false;
    }
    
    private void saveContactToMemory(Map<String, Object> contact) {
        // 保存联系人到内存
        // 实际项目中应持久化到数据库
    }
    
    private void updateContactInMemory(Map<String, Object> contact) {
        // 更新内存中的联系人
        // 实际项目中应更新数据库
    }
    
    private boolean deleteContactFromMemory(Long contactId) {
        // 从内存删除联系人
        // 实际项目中应从数据库删除
        return true;
    }
    
    private List<Map<String, Object>> searchContactsByKeyword(String keyword) {
        // 按关键词搜索联系人
        // 实际项目中应从数据库查询
        return java.util.Collections.emptyList();
    }
    
    private List<Map<String, Object>> getContactGroupsFromMemory() {
        // 获取联系人分组
        // 实际项目中应从数据库查询
        return java.util.Collections.emptyList();
    }
}