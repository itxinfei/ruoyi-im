package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImGroupBot;
import com.ruoyi.im.dto.groupbot.BotCreateRequest;
import com.ruoyi.im.dto.groupbot.BotRuleRequest;
import com.ruoyi.im.dto.groupbot.BotUpdateRequest;
import com.ruoyi.im.service.ImGroupBotService;
import com.ruoyi.im.util.BeanConvertUtil;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.group.ImGroupBotVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 群机器人控制器
 *
 * 提供群机器人的创建、查询、更新、删除等功能
 *
 * @author ruoyi
 */
@Tag(name = "群机器人", description = "群机器人管理接口")
@RestController
@RequestMapping("/api/im/groups/bots")
public class ImGroupBotController {

    private static final Logger log = LoggerFactory.getLogger(ImGroupBotController.class);

    private final ImGroupBotService groupBotService;

    public ImGroupBotController(ImGroupBotService groupBotService) {
        this.groupBotService = groupBotService;
    }

    /**
     * 将 Entity 转换为 VO
     *
     * @param bot 群机器人实体
     * @return 群机器人视图对象
     */
    private ImGroupBotVO toVO(ImGroupBot bot) {
        if (bot == null) {
            return new ImGroupBotVO();
        }
        ImGroupBotVO vo = new ImGroupBotVO();
        BeanConvertUtil.copyProperties(bot, vo);
        return vo;
    }

    /**
     * 批量将 Entity 转换为 VO
     *
     * @param list 群机器人实体列表
     * @return 群机器人视图对象列表
     */
    private List<ImGroupBotVO> toVOList(List<ImGroupBot> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    /**
     * 创建群机器人
     *
     * @param request 创建请求
     * @return 机器人ID
     */
    @Operation(summary = "创建群机器人", description = "创建新的群机器人")
    @PostMapping("/create")
    public Result<Long> create(@Valid @RequestBody BotCreateRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        log.info("创建群机器人: userId={}, groupId={}, botName={}",
                userId, request.getGroupId(), request.getBotName());
        Long botId = groupBotService.createBot(request, userId);
        return Result.success("机器人创建成功", botId);
    }

    /**
     * 更新群机器人
     *
     * @param request 更新请求
     * @return 操作结果
     */
    @Operation(summary = "更新群机器人", description = "更新群机器人信息")
    @PutMapping("/update")
    public Result<Void> update(@Valid @RequestBody BotUpdateRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        groupBotService.updateBot(request, userId);
        return Result.success("机器人更新成功");
    }

    /**
     * 删除群机器人
     *
     * @param botId 机器人ID
     * @return 操作结果
     */
    @Operation(summary = "删除群机器人", description = "删除群机器人")
    @DeleteMapping("/{botId}")
    public Result<Void> delete(@PathVariable Long botId) {
        Long userId = SecurityUtils.getLoginUserId();
        groupBotService.deleteBot(botId, userId);
        return Result.success("机器人删除成功");
    }

    /**
     * 获取群组机器人列表
     *
     * @param groupId 群组ID
     * @return 机器人列表
     */
    @Operation(summary = "获取群组机器人列表", description = "获取指定群组的机器人列表")
    @GetMapping("/list/{groupId}")
    public Result<List<ImGroupBotVO>> list(@PathVariable Long groupId) {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImGroupBot> bots = groupBotService.getGroupBots(groupId, userId);
        return Result.success(toVOList(bots));
    }

    /**
     * 获取机器人详情
     *
     * @param botId 机器人ID
     * @return 机器人详情
     */
    @Operation(summary = "获取机器人详情", description = "获取机器人详细信息")
    @GetMapping("/{botId}")
    public Result<ImGroupBotVO> getDetail(@PathVariable Long botId) {
        Long userId = SecurityUtils.getLoginUserId();
        ImGroupBot bot = groupBotService.getBotDetail(botId, userId);
        return Result.success(toVO(bot));
    }

    /**
     * 添加机器人规则
     *
     * @param botId   机器人ID
     * @param request 规则请求
     * @return 规则ID
     */
    @Operation(summary = "添加机器人规则", description = "为机器人添加新的触发规则")
    @PostMapping("/{botId}/rule")
    public Result<Long> addRule(@PathVariable Long botId, @Valid @RequestBody BotRuleRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        Long ruleId = groupBotService.addRule(botId, request, userId);
        return Result.success("规则添加成功", ruleId);
    }

    /**
     * 更新机器人规则
     *
     * @param ruleId  规则ID
     * @param request 规则请求
     * @return 操作结果
     */
    @Operation(summary = "更新机器人规则", description = "更新机器人规则")
    @PutMapping("/rule/{ruleId}")
    public Result<Void> updateRule(@PathVariable Long ruleId, @Valid @RequestBody BotRuleRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        groupBotService.updateRule(ruleId, request, userId);
        return Result.success("规则更新成功");
    }

    /**
     * 删除机器人规则
     *
     * @param ruleId 规则ID
     * @return 操作结果
     */
    @Operation(summary = "删除机器人规则", description = "删除机器人规则")
    @DeleteMapping("/rule/{ruleId}")
    public Result<Void> deleteRule(@PathVariable Long ruleId) {
        Long userId = SecurityUtils.getLoginUserId();
        groupBotService.deleteRule(ruleId, userId);
        return Result.success("规则删除成功");
    }

    /**
     * 启用/禁用机器人
     *
     * @param botId   机器人ID
     * @param enabled 是否启用
     * @return 操作结果
     */
    @Operation(summary = "设置机器人状态", description = "启用或禁用机器人")
    @PutMapping("/{botId}/enabled")
    public Result<Void> setEnabled(@PathVariable Long botId, @RequestParam Boolean enabled) {
        Long userId = SecurityUtils.getLoginUserId();
        groupBotService.setBotEnabled(botId, enabled, userId);
        return Result.success("机器人状态已更新");
    }
}
