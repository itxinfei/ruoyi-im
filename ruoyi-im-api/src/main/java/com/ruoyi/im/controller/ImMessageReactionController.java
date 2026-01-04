package com.ruoyi.im.controller;

import com.ruoyi.im.domain.ImMessageReaction;
import com.ruoyi.im.service.ImMessageReactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息互动控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/api/im/reaction")
public class ImMessageReactionController {

    @Autowired
    private ImMessageReactionService imMessageReactionService;

    /**
     * 查询消息互动列表
     */
    @GetMapping("/list")
    public Map<String, Object> list(@RequestParam(required = false) Long messageId,
                                   @RequestParam(required = false) Long userId,
                                   @RequestParam(required = false) String reactionType) {
        Map<String, Object> result = new HashMap<>();
        
        ImMessageReaction query = new ImMessageReaction();
        query.setMessageId(messageId);
        query.setUserId(userId);
        query.setReactionType(reactionType);
        
        List<ImMessageReaction> list = imMessageReactionService.selectImMessageReactionList(query);
        
        result.put("code", 200);
        result.put("msg", "查询成功");
        result.put("data", list);
        
        return result;
    }

    /**
     * 查询消息互动详细
     */
    @GetMapping("/{id}")
    public Map<String, Object> getInfo(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        ImMessageReaction reaction = imMessageReactionService.selectImMessageReactionById(id);
        
        if (reaction != null) {
            result.put("code", 200);
            result.put("msg", "查询成功");
            result.put("data", reaction);
        } else {
            result.put("code", 404);
            result.put("msg", "消息互动不存在");
        }
        
        return result;
    }

    /**
     * 查询消息的所有互动
     */
    @GetMapping("/message/{messageId}")
    public Map<String, Object> listByMessageId(@PathVariable Long messageId) {
        Map<String, Object> result = new HashMap<>();
        
        List<ImMessageReaction> list = imMessageReactionService.selectImMessageReactionByMessageId(messageId);
        
        result.put("code", 200);
        result.put("msg", "查询成功");
        result.put("data", list);
        
        return result;
    }

    /**
     * 查询用户的所有互动
     */
    @GetMapping("/user/{userId}")
    public Map<String, Object> listByUserId(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        List<ImMessageReaction> list = imMessageReactionService.selectImMessageReactionByUserId(userId);
        
        result.put("code", 200);
        result.put("msg", "查询成功");
        result.put("data", list);
        
        return result;
    }

    /**
     * 新增消息互动
     */
    @PostMapping
    public Map<String, Object> add(@RequestBody ImMessageReaction reaction) {
        Map<String, Object> result = new HashMap<>();
        
        int rows = imMessageReactionService.insertImMessageReaction(reaction);
        
        if (rows > 0) {
            result.put("code", 200);
            result.put("msg", "消息互动添加成功");
            result.put("data", reaction);
        } else {
            result.put("code", 500);
            result.put("msg", "消息互动添加失败");
        }
        
        return result;
    }

    /**
     * 添加消息互动（简化接口）
     */
    @PostMapping("/add")
    public Map<String, Object> addReaction(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        
        Long messageId = Long.valueOf(params.get("messageId").toString());
        Long userId = Long.valueOf(params.get("userId").toString());
        String reactionType = params.get("reactionType").toString();
        String emoji = params.get("emoji") != null ? params.get("emoji").toString() : null;
        
        int rows = imMessageReactionService.addReaction(messageId, userId, reactionType, emoji);
        
        if (rows > 0) {
            result.put("code", 200);
            result.put("msg", "消息互动添加成功");
        } else {
            result.put("code", 500);
            result.put("msg", "消息互动添加失败");
        }
        
        return result;
    }

    /**
     * 取消消息互动
     */
    @PostMapping("/remove")
    public Map<String, Object> removeReaction(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        
        Long messageId = Long.valueOf(params.get("messageId").toString());
        Long userId = Long.valueOf(params.get("userId").toString());
        String reactionType = params.get("reactionType").toString();
        
        int rows = imMessageReactionService.removeReaction(messageId, userId, reactionType);
        
        if (rows > 0) {
            result.put("code", 200);
            result.put("msg", "消息互动取消成功");
        } else {
            result.put("code", 500);
            result.put("msg", "消息互动取消失败");
        }
        
        return result;
    }

    /**
     * 修改消息互动
     */
    @PutMapping
    public Map<String, Object> edit(@RequestBody ImMessageReaction reaction) {
        Map<String, Object> result = new HashMap<>();
        
        int rows = imMessageReactionService.updateImMessageReaction(reaction);
        
        if (rows > 0) {
            result.put("code", 200);
            result.put("msg", "消息互动修改成功");
            result.put("data", reaction);
        } else {
            result.put("code", 500);
            result.put("msg", "消息互动修改失败");
        }
        
        return result;
    }

    /**
     * 删除消息互动
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> remove(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        int rows = imMessageReactionService.deleteImMessageReactionById(id);
        
        if (rows > 0) {
            result.put("code", 200);
            result.put("msg", "消息互动删除成功");
        } else {
            result.put("code", 404);
            result.put("msg", "消息互动不存在");
        }
        
        return result;
    }

    /**
     * 批量删除消息互动
     */
    @DeleteMapping
    public Map<String, Object> remove(@RequestBody Long[] ids) {
        Map<String, Object> result = new HashMap<>();
        
        int rows = imMessageReactionService.deleteImMessageReactionByIds(ids);
        
        if (rows > 0) {
            result.put("code", 200);
            result.put("msg", "消息互动删除成功");
        } else {
            result.put("code", 500);
            result.put("msg", "消息互动删除失败");
        }
        
        return result;
    }
}
