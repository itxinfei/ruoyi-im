package com.ruoyi.web.controller.im;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.web.domain.ImUser;
import com.ruoyi.web.service.ImUserService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * IM用户管理控制器（管理后台）
 *
 * @author ruoyi
 * @date 2025-01-07
 */
@Controller
@RequestMapping("/im/user")
public class ImUserController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ImUserController.class);

    private String prefix = "im/user";

    /**
     * 用户管理页面
     */
    @RequiresPermissions("im:user:list")
    @GetMapping()
    public String user() {
        return prefix + "/user";
    }

    @Autowired
    private ImUserService imUserService;

    /**
     * 新增用户页面
     */
    @RequiresPermissions("im:user:add")
    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

    /**
     * 修改用户页面
     */
    @RequiresPermissions("im:user:edit")
    @GetMapping("/edit/{userId}")
    public String edit(@PathVariable("userId") Long userId, org.springframework.ui.ModelMap mmap) {
        mmap.put("user", imUserService.selectImUserById(userId));
        return prefix + "/edit";
    }

    /**
     * 重置密码页面
     */
    @RequiresPermissions("im:user:edit")
    @GetMapping("/resetPassword/{userId}")
    public String resetPassword(@PathVariable("userId") Long userId, org.springframework.ui.ModelMap mmap) {
        ImUser user = imUserService.selectImUserById(userId);
        mmap.put("userId", userId);
        mmap.put("username", user != null ? user.getUsername() : "");
        return prefix + "/resetPassword";
    }

    /**
     * 查询IM用户列表
     */
    @RequiresPermissions("im:user:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ImUser imUser) {
        startPage();
        List<ImUser> list = imUserService.selectImUserList(imUser);
        return getDataTable(list);
    }

    /**
     * 获取用户统计数据
     */
    @RequiresPermissions("im:user:query")
    @GetMapping("/statistics")
    @ResponseBody
    public AjaxResult getStatistics() {
        return AjaxResult.success(imUserService.getUserStatistics());
    }

    /**
     * 获取IM用户详细信息
     */
    @RequiresPermissions("im:user:query")
    @GetMapping("/info/{id}")
    @ResponseBody
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(imUserService.selectImUserById(id));
    }

    /**
     * 新增IM用户
     */
    @RequiresPermissions("im:user:add")
    @Log(title = "IM用户", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult add(@Valid ImUser imUser) {
        return toAjax(imUserService.insertImUser(imUser));
    }

    /**
     * 修改IM用户（API接口方式，用于表格行编辑）
     */
    @RequiresPermissions("im:user:edit")
    @Log(title = "IM用户", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}")
    @ResponseBody
    public AjaxResult editApi(@PathVariable("id") Long id, @Valid @RequestBody ImUser imUser) {
        imUser.setId(id);
        return toAjax(imUserService.updateImUser(imUser));
    }

    /**
     * 修改IM用户（表单提交方式，用于编辑页面）
     */
    @RequiresPermissions("im:user:edit")
    @Log(title = "IM用户", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult edit(@Valid ImUser imUser) {
        return toAjax(imUserService.updateImUser(imUser));
    }

    /**
     * 删除IM用户
     */
    @RequiresPermissions("im:user:remove")
    @Log(title = "IM用户", businessType = BusinessType.DELETE)
    @PostMapping("/remove/{ids}")
    @ResponseBody
    public AjaxResult remove(@PathVariable String ids) {
        return toAjax(imUserService.deleteImUserByIds(Convert.toLongArray(ids)));
    }

    /**
     * 导出IM用户列表
     * @param ids 可选，指定要导出的用户ID列表（逗号分隔）
     */
    @RequiresPermissions("im:user:export")
    @Log(title = "IM用户", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ImUser imUser, String ids) {
        List<ImUser> list;

        // 如果指定了ids，则只导出选中的用户
        if (ids != null && !ids.trim().isEmpty()) {
            String[] idArray = ids.split(",");
            Long[] userIds = new Long[idArray.length];
            for (int i = 0; i < idArray.length; i++) {
                try {
                    userIds[i] = Long.parseLong(idArray[i].trim());
                } catch (NumberFormatException e) {
                    continue;
                }
            }
            list = imUserService.selectImUserByIds(userIds);
        } else {
            // 否则导出所有符合条件的用户
            list = imUserService.selectImUserList(imUser);
        }

        ExcelUtil<ImUser> util = new ExcelUtil<>(ImUser.class);
        util.exportExcel(response, list, "用户数据");
    }

    /**
     * 重置用户密码
     */
    @RequiresPermissions("im:user:edit")
    @Log(title = "重置用户密码", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/reset-password")
    @ResponseBody
    public AjaxResult resetPassword(@PathVariable("id") Long id, @RequestBody Map<String, String> params) {
        String newPassword = params.get("password");
        String adminPassword = params.get("adminPassword");

        if (newPassword == null || newPassword.trim().isEmpty()) {
            return AjaxResult.error("新密码不能为空");
        }
        if (newPassword.length() < 6) {
            return AjaxResult.error("密码长度不能少于6位");
        }
        if (newPassword.length() > 20) {
            return AjaxResult.error("密码长度不能超过20位");
        }

        if (adminPassword == null || adminPassword.trim().isEmpty()) {
            return AjaxResult.error("管理员密码不能为空");
        }

        int result = imUserService.resetPassword(id, newPassword, adminPassword);
        if (result > 0) {
            return AjaxResult.success("密码重置成功");
        } else if (result == -1) {
            return AjaxResult.error("管理员密码验证失败");
        } else if (result == -2) {
            return AjaxResult.error("目标用户不存在");
        } else {
            return AjaxResult.error("密码重置失败");
        }
    }

    /**
     * 启用/禁用用户
     */
    @RequiresPermissions("im:user:edit")
    @Log(title = "修改用户状态", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/status")
    @ResponseBody
    public AjaxResult changeStatus(@PathVariable("id") Long id, @RequestParam String status) {
        return toAjax(imUserService.changeStatus(id, status));
    }

    /**
     * 批量启用/停用用户
     */
    @RequiresPermissions("im:user:edit")
    @Log(title = "批量修改用户状态", businessType = BusinessType.UPDATE)
    @PostMapping("/batchUpdateStatus")
    @ResponseBody
    public AjaxResult batchUpdateStatus(@RequestParam String userIds, @RequestParam String status) {
        if (userIds == null || userIds.trim().isEmpty()) {
            return AjaxResult.error("请选择要修改的用户");
        }
        String[] ids = userIds.split(",");
        Long[] userIdArray = new Long[ids.length];
        for (int i = 0; i < ids.length; i++) {
            try {
                userIdArray[i] = Long.parseLong(ids[i].trim());
            } catch (NumberFormatException e) {
                return AjaxResult.error("用户ID格式错误");
            }
        }
        int count = imUserService.batchUpdateStatus(userIdArray, status);
        return AjaxResult.success("成功修改 " + count + " 个用户的状态");
    }

    /**
     * 获取用户在线状态
     */
    @RequiresPermissions("im:user:query")
    @GetMapping("/online-status")
    @ResponseBody
    public AjaxResult getOnlineStatus() {
        return AjaxResult.success(imUserService.countOnlineUsers());
    }

    /**
     * 搜索用户
     */
    @RequiresPermissions("im:user:list")
    @GetMapping("/search")
    @ResponseBody
    public AjaxResult searchUsers(@RequestParam String keyword) {
        List<ImUser> list = imUserService.searchUsers(keyword);
        return AjaxResult.success(list);
    }

    /**
     * 导入用户数据页面
     */
    @RequiresPermissions("im:user:import")
    @GetMapping("/importTemplate")
    @ResponseBody
    public AjaxResult importTemplate() {
        ExcelUtil<ImUser> util = new ExcelUtil<>(ImUser.class);
        return util.importTemplateExcel("用户数据");
    }

    /**
     * 批量导入用户数据
     *
     * @param file 上传的Excel文件
     * @param updateSupported 是否支持更新已存在的用户
     */
    @RequiresPermissions("im:user:import")
    @Log(title = "IM用户", businessType = BusinessType.IMPORT)
    @PostMapping("/importData")
    @ResponseBody
    public AjaxResult importData(@RequestParam("file") MultipartFile file,
                                  @RequestParam(value = "updateSupported", defaultValue = "false") boolean updateSupported) {
        if (file.isEmpty()) {
            return AjaxResult.error("请选择要上传的文件");
        }

        try {
            ExcelUtil<ImUser> util = new ExcelUtil<>(ImUser.class);
            List<ImUser> userList = util.importExcel(file.getInputStream());

            if (userList == null || userList.isEmpty()) {
                return AjaxResult.error("导入文件中没有有效的用户数据");
            }

            // 调用 Service 进行批量导入
            Map<String, Object> result = imUserService.batchImportUsers(userList, updateSupported);

            return AjaxResult.success(result);

        } catch (Exception e) {
            logger.error("导入用户数据失败", e);
            return AjaxResult.error("导入失败: " + e.getMessage());
        }
    }
}
