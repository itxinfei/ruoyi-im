package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImExternalContact;
import com.ruoyi.im.domain.ImExternalContactGroup;
import com.ruoyi.im.dto.contact.ExternalContactCreateRequest;
import com.ruoyi.im.service.ImExternalContactService;
import com.ruoyi.im.util.BeanConvertUtil;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.contact.ImExternalContactGroupVO;
import com.ruoyi.im.vo.contact.ImExternalContactVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 外部联系人管理控制器
 *
 * @author ruoyi
 */
@Tag(name = "外部联系人", description = "外部联系人管理接口")
@RestController
@RequestMapping("/api/im/external-contacts")
public class ImExternalContactController {

    private final ImExternalContactService externalContactService;

    /**
     * 构造器注入依赖
     *
     * @param externalContactService 外部联系人服务
     */
    public ImExternalContactController(ImExternalContactService externalContactService) {
        this.externalContactService = externalContactService;
    }

    /**
     * 将 Entity 转换为 VO
     *
     * @param contact 外部联系人实体
     * @return 外部联系人视图对象
     */
    private ImExternalContactVO toVO(ImExternalContact contact) {
        if (contact == null) {
            return new ImExternalContactVO();
        }
        ImExternalContactVO vo = new ImExternalContactVO();
        BeanConvertUtil.copyProperties(contact, vo);
        return vo;
    }

    /**
     * 批量将 Entity 转换为 VO
     *
     * @param list 外部联系人实体列表
     * @return 外部联系人视图对象列表
     */
    private List<ImExternalContactVO> toVOList(List<ImExternalContact> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    /**
     * 将分组 Entity 转换为 VO
     *
     * @param group 分组实体
     * @return 分组视图对象
     */
    private ImExternalContactGroupVO toGroupVO(ImExternalContactGroup group) {
        if (group == null) {
            return new ImExternalContactGroupVO();
        }
        ImExternalContactGroupVO vo = new ImExternalContactGroupVO();
        BeanConvertUtil.copyProperties(group, vo);
        return vo;
    }

    /**
     * 批量将分组 Entity 转换为 VO
     *
     * @param list 分组实体列表
     * @return 分组视图对象列表
     */
    private List<ImExternalContactGroupVO> toGroupVOList(List<ImExternalContactGroup> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream()
                .map(this::toGroupVO)
                .collect(Collectors.toList());
    }

    // ==================== 联系人管理 ====================

    /**
     * 创建联系人
     */
    @Operation(summary = "创建联系人")
    @PostMapping
    public Result<Long> createContact(
            @Valid @RequestBody ExternalContactCreateRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        Long contactId = externalContactService.createContact(request, userId);
        return Result.success("创建成功", contactId);
    }

    /**
     * 更新联系人
     */
    @Operation(summary = "更新联系人")
    @PutMapping("/{contactId}")
    public Result<Void> updateContact(
            @PathVariable Long contactId,
            @Valid @RequestBody ExternalContactCreateRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        externalContactService.updateContact(contactId, request, userId);
        return Result.success("更新成功");
    }

    /**
     * 删除联系人
     */
    @Operation(summary = "删除联系人")
    @DeleteMapping("/{contactId}")
    public Result<Void> deleteContact(
            @PathVariable Long contactId) {
        Long userId = SecurityUtils.getLoginUserId();
        externalContactService.deleteContact(contactId, userId);
        return Result.success("删除成功");
    }

    /**
     * 获取联系人详情
     */
    @Operation(summary = "获取联系人详情")
    @GetMapping("/{contactId}")
    public Result<ImExternalContactVO> getContactDetail(
            @PathVariable Long contactId) {
        Long userId = SecurityUtils.getLoginUserId();
        ImExternalContact contact = externalContactService.getContactDetail(contactId, userId);
        return Result.success(toVO(contact));
    }

    /**
     * 获取联系人列表
     */
    @Operation(summary = "获取联系人列表")
    @GetMapping("/list")
    public Result<List<ImExternalContactVO>> getContactList() {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImExternalContact> list = externalContactService.getContactList(userId);
        return Result.success(toVOList(list));
    }

    /**
     * 获取分组下的联系人列表
     */
    @Operation(summary = "获取分组下的联系人列表")
    @GetMapping("/group/{groupId}")
    public Result<List<ImExternalContactVO>> getContactsByGroup(
            @PathVariable Long groupId) {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImExternalContact> list = externalContactService.getContactsByGroup(groupId, userId);
        return Result.success(toVOList(list));
    }

    /**
     * 获取星标联系人列表
     */
    @Operation(summary = "获取星标联系人列表")
    @GetMapping("/starred")
    public Result<List<ImExternalContactVO>> getStarredContacts() {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImExternalContact> list = externalContactService.getStarredContacts(userId);
        return Result.success(toVOList(list));
    }

    /**
     * 搜索联系人
     */
    @Operation(summary = "搜索联系人")
    @GetMapping("/search")
    public Result<List<ImExternalContactVO>> searchContacts(
            @RequestParam String keyword) {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImExternalContact> list = externalContactService.searchContacts(keyword, userId);
        return Result.success(toVOList(list));
    }

    /**
     * 切换星标状态
     */
    @Operation(summary = "切换星标状态")
    @PutMapping("/{contactId}/star")
    public Result<Void> toggleStarred(
            @PathVariable Long contactId) {
        Long userId = SecurityUtils.getLoginUserId();
        externalContactService.toggleStarred(contactId, userId);
        return Result.success("操作成功");
    }

    // ==================== 分组管理 ====================

    /**
     * 创建分组
     */
    @Operation(summary = "创建分组")
    @PostMapping("/group")
    public Result<Long> createGroup(
            @RequestParam String name) {
        Long userId = SecurityUtils.getLoginUserId();
        Long groupId = externalContactService.createGroup(name, userId);
        return Result.success("创建成功", groupId);
    }

    /**
     * 更新分组
     */
    @Operation(summary = "更新分组")
    @PutMapping("/group/{groupId}")
    public Result<Void> updateGroup(
            @PathVariable Long groupId,
            @RequestParam String name) {
        Long userId = SecurityUtils.getLoginUserId();
        externalContactService.updateGroup(groupId, name, userId);
        return Result.success("更新成功");
    }

    /**
     * 删除分组
     */
    @Operation(summary = "删除分组")
    @DeleteMapping("/group/{groupId}")
    public Result<Void> deleteGroup(
            @PathVariable Long groupId) {
        Long userId = SecurityUtils.getLoginUserId();
        externalContactService.deleteGroup(groupId, userId);
        return Result.success("删除成功");
    }

    /**
     * 获取分组列表
     */
    @Operation(summary = "获取分组列表")
    @GetMapping("/group/list")
    public Result<List<ImExternalContactGroupVO>> getGroupList() {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImExternalContactGroup> list = externalContactService.getGroupList(userId);
        return Result.success(toGroupVOList(list));
    }
}
