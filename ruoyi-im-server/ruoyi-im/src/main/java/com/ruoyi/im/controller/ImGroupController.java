package com.ruoyi.im.controller;

import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.im.domain.ImGroup;
import com.ruoyi.im.service.IImGroupService;

/**
 * IM群组Controller
 * 
 * @author ruoyi
 * @date 2024-08-09
 */
@RestController
@RequestMapping("/im/group")
public class ImGroupController extends BaseController
{
    @Autowired
    private IImGroupService imGroupService;

    /**
     * 查询IM群组列表
     */
    @PreAuthorize("@ss.hasPermi('im:group:list')")
    @GetMapping("/list")
    public TableDataInfo list(ImGroup imGroup)
    {
        startPage();
        List<ImGroup> list = imGroupService.list();
        return getDataTable(list);
    }

    /**
     * 导出IM群组列表
     */
    @PreAuthorize("@ss.hasPermi('im:group:export')")
    @Log(title = "IM群组", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ImGroup imGroup)
    {
        List<ImGroup> list = imGroupService.list();
        ExcelUtil<ImGroup> util = new ExcelUtil<ImGroup>(ImGroup.class);
        util.exportExcel(response, list, "IM群组数据");
    }

    /**
     * 获取IM群组详细信息
     */
    @PreAuthorize("@ss.hasPermi('im:group:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(imGroupService.getById(id));
    }

    /**
     * 新增IM群组
     */
    @PreAuthorize("@ss.hasPermi('im:group:add')")
    @Log(title = "IM群组", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ImGroup imGroup)
    {
        return toAjax(imGroupService.save(imGroup));
    }

    /**
     * 修改IM群组
     */
    @PreAuthorize("@ss.hasPermi('im:group:edit')")
    @Log(title = "IM群组", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ImGroup imGroup)
    {
        return toAjax(imGroupService.updateById(imGroup));
    }

    /**
     * 删除IM群组
     */
    @PreAuthorize("@ss.hasPermi('im:group:remove')")
    @Log(title = "IM群组", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(imGroupService.removeBatchByIds(Arrays.asList(ids)));
    }

    /**
     * 创建群组
     */
    @PostMapping("/create")
    public AjaxResult createGroup(@RequestBody ImGroup group)
    {
        return toAjax(imGroupService.createGroup(group, Arrays.asList(getCurrentUserId())));
    }

    /**
     * 解散群组
     */
    @PostMapping("/dissolve/{groupId}")
    public AjaxResult dissolveGroup(@PathVariable Long groupId)
    {
        return toAjax(imGroupService.dissolveGroup(groupId, getCurrentUserId()));
    }

    /**
     * 更新群组信息
     */
    @PutMapping("/update")
    public AjaxResult updateGroupInfo(@RequestBody ImGroup group)
    {
        return toAjax(imGroupService.updateGroup(group, getCurrentUserId()));
    }

    /**
     * 查询用户的群组
     */
    @GetMapping("/user")
    public AjaxResult getUserGroups()
    {
        List<ImGroup> groups = imGroupService.selectUserGroups(getCurrentUserId());
        return success(groups);
    }

    /**
     * 获取群组详情
     */
    @GetMapping("/detail/{groupId}")
    public AjaxResult getGroupDetail(@PathVariable Long groupId)
    {
        ImGroup group = imGroupService.getGroupDetail(groupId, getCurrentUserId());
        return success(group);
    }

    /**
     * 搜索群组
     */
    @GetMapping("/search")
    public AjaxResult searchGroups(String keyword)
    {
        List<ImGroup> groups = imGroupService.searchGroups(keyword, getCurrentUserId());
        return success(groups);
    }

    /**
     * 检查是否为群主
     */
    @GetMapping("/checkOwner/{groupId}")
    public AjaxResult checkGroupOwner(@PathVariable Long groupId)
    {
        boolean isOwner = imGroupService.isGroupOwner(groupId, getCurrentUserId());
        return success(isOwner);
    }

    /**
     * 检查是否为管理员
     */
    @GetMapping("/checkAdmin/{groupId}")
    public AjaxResult checkGroupAdmin(@PathVariable Long groupId)
    {
        boolean isAdmin = imGroupService.isGroupAdmin(groupId, getCurrentUserId());
        return success(isAdmin);
    }

    /**
     * 转让群主
     */
    @PostMapping("/transfer/{groupId}/{newOwnerId}")
    public AjaxResult transferOwnership(@PathVariable Long groupId, @PathVariable Long newOwnerId)
    {
        return toAjax(imGroupService.transferOwnership(groupId, newOwnerId, getCurrentUserId()));
    }

    /**
     * 更新成员数量
     */
    @PostMapping("/updateMemberCount/{groupId}")
    public AjaxResult updateMemberCount(@PathVariable Long groupId)
    {
        return toAjax(imGroupService.updateMemberCount(groupId));
    }

    /**
     * 批量查询群组
     */
    @PostMapping("/batchQuery")
    public AjaxResult batchQueryGroups(@RequestBody List<Long> groupIds)
    {
        List<ImGroup> groups = imGroupService.selectGroupsByIds(groupIds);
        return success(groups);
    }

    /**
     * 查询用户创建的群组
     */
    @GetMapping("/owned")
    public AjaxResult getOwnedGroups()
    {
        List<ImGroup> groups = imGroupService.selectGroupsByOwner(getCurrentUserId());
        return success(groups);
    }

    /**
     * 查询活跃群组
     */
    @GetMapping("/active")
    public AjaxResult getActiveGroups()
    {
        List<ImGroup> groups = imGroupService.selectActiveGroups(10);
        return success(groups);
    }

    /**
     * 统计用户群组数量
     */
    @GetMapping("/count")
    public AjaxResult countUserGroups()
    {
        int count = imGroupService.countUserGroups(getCurrentUserId());
        return success(count);
    }

    /**
     * 检查群组是否活跃
     */
    @GetMapping("/checkActive/{groupId}")
    public AjaxResult checkGroupActive(@PathVariable Long groupId)
    {
        boolean isActive = imGroupService.isGroupActive(groupId);
        return success(isActive);
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId()
    {
        return getUserId();
    }
}