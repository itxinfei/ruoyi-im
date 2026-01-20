package com.ruoyi.web.controller.im;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.web.domain.ImMessage;
import com.ruoyi.web.service.ImMessageService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IM消息管理控制器（管理后台）
 *
 * @author ruoyi
 * @date 2025-01-07
 */
@Controller
@RequestMapping("/im/message")
public class ImMessageController extends BaseController {

    private String prefix = "im/message";

    @Autowired
    private ImMessageService imMessageService;

    /**
     * 消息管理页面
     */
    @RequiresPermissions("im:message:list")
    @GetMapping()
    public String message() {
        return prefix + "/message";
    }

    /**
     * 新增消息页面
     */
    @RequiresPermissions("im:message:add")
    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

    /**
     * 修改消息页面
     */
    @RequiresPermissions("im:message:edit")
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, org.springframework.ui.ModelMap mmap) {
        mmap.put("message", imMessageService.selectImMessageById(id));
        return prefix + "/edit";
    }

    /**
     * 查询IM消息列表
     */
    @RequiresPermissions("im:message:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ImMessage imMessage) {
        startPage();
        List<ImMessage> list = imMessageService.selectImMessageList(imMessage);
        return getDataTable(list);
    }

    /**
     * 导出IM消息列表
     */
    @RequiresPermissions("im:message:export")
    @Log(title = "IM消息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ImMessage imMessage) {
        List<ImMessage> list = imMessageService.selectImMessageList(imMessage);
        ExcelUtil<ImMessage> util = new ExcelUtil<>(ImMessage.class);
        util.exportExcel(response, list, "消息数据");
    }

    /**
     * 获取IM消息详细信息
     */
    @RequiresPermissions("im:message:query")
    @GetMapping("/info/{id}")
    @ResponseBody
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(imMessageService.selectImMessageById(id));
    }

    /**
     * 新增IM消息
     */
    @RequiresPermissions("im:message:add")
    @Log(title = "IM消息", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult add(ImMessage imMessage) {
        return toAjax(imMessageService.insertImMessage(imMessage));
    }

    /**
     * 获取消息详情（匹配前端调用 /im/message/{id}）
     */
    @RequiresPermissions("im:message:query")
    @GetMapping("/{id}")
    @ResponseBody
    public AjaxResult getMessageDetail(@PathVariable("id") Long id) {
        ImMessage message = imMessageService.selectImMessageById(id);
        if (message != null && message.getContent() != null) {
            // 解密消息内容
            String decryptedContent = decryptMessage(message.getContent());
            message.setContent(decryptedContent);
            // 如果有编辑后的内容，也需要解密
            if (message.getEditedContent() != null) {
                String decryptedEditedContent = decryptMessage(message.getEditedContent());
                message.setEditedContent(decryptedEditedContent);
            }
        }
        return AjaxResult.success(message);
    }

    /**
     * 处理消息内容 - 管理员可直接查看
     * 尝试各种编码格式处理乱码
     */
    private String decryptMessage(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        // 1. 尝试Base64解码
        try {
            byte[] decoded = java.util.Base64.getDecoder().decode(content);
            String decodedStr = new String(decoded, java.nio.charset.StandardCharsets.UTF_8);
            // 检查解码后是否包含可读字符
            if (isReadableText(decodedStr)) {
                return decodedStr;
            }
        } catch (Exception e) {
            // 不是Base64，继续其他尝试
        }

        // 2. 检查是否为十六进制编码（格式：67677c...）
        if (content.matches("^[0-9a-fA-F]+$") && content.length() > 20) {
            try {
                byte[] hex = hexStringToBytes(content);
                String hexStr = new String(hex, java.nio.charset.StandardCharsets.UTF_8);
                if (isReadableText(hexStr)) {
                    return hexStr;
                }
            } catch (Exception e) {
                // 继续尝试其他方法
            }
        }

        // 3. 尝试GBK/GB2312解码（中文乱码常见原因）
        try {
            byte[] bytes = content.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
            String gbkStr = new String(bytes, java.nio.charset.Charset.forName("GBK"));
            if (isReadableText(gbkStr) && containsChinese(gbkStr)) {
                return gbkStr;
            }
        } catch (Exception e) {
            // 继续其他尝试
        }

        // 4. 尝试GB2312解码
        try {
            byte[] bytes = content.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
            String gb2312Str = new String(bytes, java.nio.charset.Charset.forName("GB2312"));
            if (isReadableText(gb2312Str) && containsChinese(gb2312Str)) {
                return gb2312Str;
            }
        } catch (Exception e) {
            // 继续其他尝试
        }

        // 5. 检查是否为URL编码
        try {
            String urlDecoded = java.net.URLDecoder.decode(content, "UTF-8");
            if (isReadableText(urlDecoded) && !urlDecoded.equals(content)) {
                return urlDecoded;
            }
        } catch (Exception e) {
            // 继续尝试其他方法
        }

        // 6. 如果内容太长且看起来像Base64，提示这是加密消息
        if (content.length() > 50 && isBase64Format(content)) {
            return "[加密消息] " + content.substring(0, 20) + "...]";
        }

        // 7. 否则返回原内容
        return content;
    }

    /**
     * 判断是否为Base64格式
     */
    private boolean isBase64Format(String content) {
        if (content == null || content.length() < 20) {
            return false;
        }
        // Base64字符：A-Z, a-z, 0-9, +, /, =
        return content.matches("^[A-Za-z0-9+/=]+$");
    }

    /**
     * 判断内容是否为可读文本
     */
    private boolean isReadableText(String text) {
        if (text == null || text.length() < 5) {
            return false;
        }
        // 检查是否包含可读字符（字母、数字、常用符号、中文字符）
        return text.matches(".*[\\p{Alnum}\\p{Punct}\\s\\u4e00-\\u9fff]+.*");
    }

    /**
     * 判断字符串是否包含中文字符
     */
    private boolean containsChinese(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return text.matches(".*[\\u4e00-\\u9fff]+.*");
    }

    /**
     * 将十六进制字符串转换为字节数组
     */
    private byte[] hexStringToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            int high = hexCharToInt(s.charAt(i));
            int low = hexCharToInt(s.charAt(i + 1));
            data[i / 2] = (byte) ((high != -1 ? 0 : high) * 16 + (low != -1 ? 0 : low));
        }
        return data;
    }

    /**
     * 将十六进制字符转换为整数值
     */
    private int hexCharToInt(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'a' && c <= 'f') {
            return 10 + (c - 'a');
        }
        if (c >= 'A' && c <= 'F') {
            return 10 + (c - 'A');
        }
        return -1;
    }

    /**
     * 修改IM消息（表单提交方式，用于编辑页面）
     */
    @RequiresPermissions("im:message:edit")
    @Log(title = "IM消息", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult edit(ImMessage imMessage) {
        return toAjax(imMessageService.updateImMessage(imMessage));
    }

    /**
     * 撤回消息
     */
    @RequiresPermissions("im:message:edit")
    @Log(title = "撤回消息", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/revoke")
    @ResponseBody
    public AjaxResult revoke(@PathVariable("id") Long messageId) {
        return toAjax(imMessageService.revokeMessage(messageId));
    }

    /**
     * 批量撤回消息
     */
    @RequiresPermissions("im:message:edit")
    @Log(title = "批量撤回消息", businessType = BusinessType.UPDATE)
    @PostMapping("/batchRevoke")
    @ResponseBody
    public AjaxResult batchRevoke(@RequestParam String messageIds) {
        if (messageIds == null || messageIds.trim().isEmpty()) {
            return AjaxResult.error("请选择要撤回的消息");
        }
        String[] ids = messageIds.split(",");
        Long[] messageIdArray = new Long[ids.length];
        for (int i = 0; i < ids.length; i++) {
            try {
                messageIdArray[i] = Long.parseLong(ids[i].trim());
            } catch (NumberFormatException e) {
                return AjaxResult.error("消息ID格式错误");
            }
        }
        int count = imMessageService.batchRevokeMessages(messageIdArray);
        return AjaxResult.success("成功撤回 " + count + " 条消息");
    }

    /**
     * 标记消息为敏感
     */
    @RequiresPermissions("im:message:edit")
    @Log(title = "标记敏感消息", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/mark-sensitive")
    @ResponseBody
    public AjaxResult markSensitive(@PathVariable("id") Long messageId) {
        ImMessage message = new ImMessage();
        message.setId(messageId);
        message.setSensitiveLevel("SENSITIVE");
        return toAjax(imMessageService.updateImMessage(message));
    }

    /**
     * 批量标记消息为敏感
     */
    @RequiresPermissions("im:message:edit")
    @Log(title = "批量标记敏感消息", businessType = BusinessType.UPDATE)
    @PostMapping("/batch-mark-sensitive")
    @ResponseBody
    public AjaxResult batchMarkSensitive(@RequestBody List<Long> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) {
            return AjaxResult.error("请选择要标记的消息");
        }
        int successCount = imMessageService.batchUpdateSensitiveLevel(messageIds, "SENSITIVE");
        return AjaxResult.success("成功标记 " + successCount + " 条消息");
    }

    /**
     * 取消标记敏感消息
     */
    @RequiresPermissions("im:message:edit")
    @Log(title = "取消标记敏感", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/unmark-sensitive")
    @ResponseBody
    public AjaxResult unmarkSensitive(@PathVariable("id") Long messageId) {
        ImMessage message = new ImMessage();
        message.setId(messageId);
        message.setSensitiveLevel("NORMAL");
        return toAjax(imMessageService.updateImMessage(message));
    }

    /**
     * 批量取消标记敏感消息
     */
    @RequiresPermissions("im:message:edit")
    @Log(title = "批量取消标记敏感", businessType = BusinessType.UPDATE)
    @PostMapping("/batch-unmark-sensitive")
    @ResponseBody
    public AjaxResult batchUnmarkSensitive(@RequestBody List<Long> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) {
            return AjaxResult.error("请选择要取消标记的消息");
        }
        int successCount = imMessageService.batchUpdateSensitiveLevel(messageIds, "NORMAL");
        return AjaxResult.success("成功取消标记 " + successCount + " 条消息");
    }

    /**
     * 删除IM消息
     */
    @RequiresPermissions("im:message:remove")
    @Log(title = "删除IM消息", businessType = BusinessType.DELETE)
    @PostMapping("/remove/{ids}")
    @ResponseBody
    public AjaxResult remove(@PathVariable String ids) {
        return toAjax(imMessageService.deleteImMessageByIds(Convert.toLongArray(ids)));
    }

    /**
     * 获取会话消息列表
     */
    @RequiresPermissions("im:message:list")
    @GetMapping("/conversation/{conversationId}")
    @ResponseBody
    public AjaxResult getByConversation(@PathVariable("conversationId") Long conversationId,
                                      @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                      @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize) {
        List<ImMessage> list = imMessageService.selectImMessageListByConversationId(conversationId);
        return AjaxResult.success(list);
    }

    /**
     * 按时间范围查询消息
     */
    @RequiresPermissions("im:message:list")
    @GetMapping("/conversation/{conversationId}/range")
    @ResponseBody
    public AjaxResult getByTimeRange(@PathVariable("conversationId") Long conversationId,
                                    @RequestParam String startTime,
                                    @RequestParam String endTime) {
        List<ImMessage> list = imMessageService.selectImMessageListByTimeRange(conversationId, startTime, endTime);
        return AjaxResult.success(list);
    }

    /**
     * 获取消息统计数据
     */
    @RequiresPermissions("im:message:list")
    @GetMapping("/statistics")
    @ResponseBody
    public AjaxResult getStatistics() {
        // 调用 Service 获取统计数据
        Map<String, Object> dbStats = imMessageService.getMessageStatistics();

        // 转换字段名为驼峰格式（前端期望）
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", dbStats.get("total_count"));
        stats.put("todayCount", dbStats.get("today_count"));
        stats.put("sensitiveCount", dbStats.get("sensitive_count"));
        stats.put("failedCount", dbStats.get("failed_count"));
        return AjaxResult.success(stats);
    }

    /**
     * 敏感消息统计
     */
    @RequiresPermissions("im:message:list")
    @GetMapping("/statistics/sensitive")
    @ResponseBody
    public AjaxResult getSensitiveStatistics() {
        // 调用 Service 获取统计数据
        Map<String, Object> dbStats = imMessageService.getMessageStatistics();

        // 转换字段名为驼峰格式（前端期望）
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", dbStats.get("total_count"));
        stats.put("normalCount", dbStats.get("normal_count"));
        stats.put("sensitiveCount", dbStats.get("sensitive_count"));
        stats.put("highCount", dbStats.get("high_count"));
        return AjaxResult.success(stats);
    }
}
