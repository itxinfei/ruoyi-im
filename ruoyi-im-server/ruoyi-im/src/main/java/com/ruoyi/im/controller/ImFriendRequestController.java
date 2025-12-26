package com.ruoyi.im.controller;

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
import com.ruoyi.im.domain.ImFriendRequest;
import com.ruoyi.im.service.IImFriendRequestService;

/**
 * IM好友请求Controller
 * 
 * @author ruoyi
 * @date 2024-08-09
 */
@RestController
@RequestMapping("/im/friendRequest")
public class ImFriendRequestController extends BaseController
{
    @Autowired
    private IImFriendRequestService imFriendRequestService;

    /**
     * 查询IM好友请求列表
     */
    @PreAuthorize("@ss.hasPermi('im:friendRequest:list')")
    @GetMapping("/list")
    public TableDataInfo list(ImFriendRequest imFriendRequest)
    {
        startPage();
        List<ImFriendRequest> list = imFriendRequestService.list();
        return getDataTable(list);
    }

    /**
     * 导出IM好友请求列表
     */
    @PreAuthorize("@ss.hasPermi('im:friendRequest:export')")
    @Log(title = "IM好友请求", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ImFriendRequest imFriendRequest)
    {
        List<ImFriendRequest> list = imFriendRequestService.list();
        ExcelUtil<ImFriendRequest> util = new ExcelUtil<ImFriendRequest>(ImFriendRequest.class);
        util.exportExcel(response, list, "IM好友请求数据");
    }

    /**
     * 获取IM好友请求详细信息
     */
    @PreAuthorize("@ss.hasPermi('im:friendRequest:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(imFriendRequestService.getById(id));
    }

    /**
     * 新增IM好友请求
     */
    @PreAuthorize("@ss.hasPermi('im:friendRequest:add')")
    @Log(title = "IM好友请求", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ImFriendRequest imFriendRequest)
    {
        return toAjax(imFriendRequestService.save(imFriendRequest) ? 1 : 0);
    }

    /**
     * 修改IM好友请求
     */
    @PreAuthorize("@ss.hasPermi('im:friendRequest:edit')")
    @Log(title = "IM好友请求", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ImFriendRequest imFriendRequest)
    {
        return toAjax(imFriendRequestService.updateById(imFriendRequest) ? 1 : 0);
    }

    /**
     * 删除IM好友请求
     */
    @PreAuthorize("@ss.hasPermi('im:friendRequest:remove')")
    @Log(title = "IM好友请求", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(imFriendRequestService.removeByIds(java.util.Arrays.asList(ids)) ? 1 : 0);
    }

    /**
     * 发送好友请求
     */
    @PostMapping("/send")
    public AjaxResult sendFriendRequest(@RequestBody ImFriendRequest request)
    {
        return toAjax(imFriendRequestService.sendFriendRequest(getCurrentUserId(), request.getToUserId(), request.getMessage()) ? 1 : 0);
    }

    /**
     * 处理好友请求
     */
    @PostMapping("/handle/{requestId}")
    public AjaxResult handleFriendRequest(@PathVariable Long requestId, @RequestBody ImFriendRequest request)
    {
        return toAjax(imFriendRequestService.handleFriendRequest(requestId, getCurrentUserId(), request.getStatus()) ? 1 : 0);
    }

    /**
     * 批量处理好友请求
     */
    @PostMapping("/batchHandle")
    public AjaxResult batchHandleFriendRequests(@RequestBody List<Long> requestIds, String status)
    {
        return toAjax(imFriendRequestService.handleFriendRequestBatch(requestIds, getCurrentUserId(), status));
    }

    /**
     * 查询收到的好友请求
     */
    @GetMapping("/received")
    public AjaxResult getReceivedRequests()
    {
        List<ImFriendRequest> requests = imFriendRequestService.selectReceivedRequests(getCurrentUserId());
        return success(requests);
    }

    /**
     * 查询发送的好友请求
     */
    @GetMapping("/sent")
    public AjaxResult getSentRequests()
    {
        List<ImFriendRequest> requests = imFriendRequestService.selectSentRequests(getCurrentUserId());
        return success(requests);
    }

    /**
     * 查询待处理的好友请求
     */
    @GetMapping("/pending")
    public AjaxResult getPendingRequests()
    {
        List<ImFriendRequest> requests = imFriendRequestService.selectReceivedRequests(getCurrentUserId());
        return success(requests);
    }

    /**
     * 统计待处理请求数量
     */
    @GetMapping("/pendingCount")
    public AjaxResult countPendingRequests()
    {
        int count = imFriendRequestService.countPendingRequests(getCurrentUserId());
        return success(count);
    }

    /**
     * 撤回好友请求
     */
    @PostMapping("/withdraw/{requestId}")
    public AjaxResult withdrawFriendRequest(@PathVariable Long requestId)
    {
        return toAjax(imFriendRequestService.withdrawFriendRequest(requestId, getCurrentUserId()) ? 1 : 0);
    }

    /**
     * 检查是否有待处理请求
     */
    @GetMapping("/checkPending/{userId}")
    public AjaxResult checkPendingRequest(@PathVariable Long userId)
    {
        boolean hasPending = imFriendRequestService.hasPendingRequest(getCurrentUserId(), userId);
        return success(hasPending);
    }

    /**
     * 清理过期请求
     */
    @PostMapping("/cleanExpired")
    public AjaxResult cleanExpiredRequests()
    {
        return toAjax(imFriendRequestService.cleanExpiredRequests(30));
    }

    /**
     * 获取请求详情
     */
    @GetMapping("/detail/{requestId}")
    public AjaxResult getRequestDetail(@PathVariable Long requestId)
    {
        ImFriendRequest detail = imFriendRequestService.getRequestInfo(requestId);
        return success(detail);
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId()
    {
        return getUserId();
    }
}