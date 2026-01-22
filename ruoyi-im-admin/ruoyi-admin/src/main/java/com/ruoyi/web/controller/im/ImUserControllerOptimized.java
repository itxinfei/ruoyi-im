package com.ruoyi.web.controller.im;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.web.domain.ImUser;
import com.ruoyi.web.domain.dto.ImUserCreateDTO;
import com.ruoyi.web.domain.dto.ImUserUpdateDTO;
import com.ruoyi.web.domain.vo.ImUserVO;
import com.ruoyi.web.service.IImUserService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

/**
 * IM用户基础管理控制器
 * 
 * @author ruoyi
 * @version 1.0
 * @since 2025-01-22
 */
@Controller
@RequestMapping("/im/user")
public class ImUserController extends BaseController {

    private String prefix = "im/user";

    @Autowired
    private IImUserService imUserService;

    @RequiresPermissions("im:user:view")
    @GetMapping()
    public String user() {
        return prefix + "/user";
    }

    @RequiresPermissions("im:user:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ImUser imUser) {
        startPage();
        List<ImUser> list = imUserService.selectImUserList(imUser);
        List<ImUserVO> voList = list.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return getDataTable(voList);
    }

    @Log(title = "IM用户", businessType = BusinessType.EXPORT)
    @RequiresPermissions("im:user:export")
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(ImUser imUser) {
        List<ImUser> list = imUserService.selectImUserList(imUser);
        ExcelUtil<ImUser> util = new ExcelUtil<>(ImUser.class);
        return util.exportExcel(list, "IM用户数据");
    }

    @RequiresPermissions("im:user:view")
    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

    @Log(title = "IM用户", businessType = BusinessType.INSERT)
    @RequiresPermissions("im:user:add")
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult add(@Validated @RequestBody ImUserCreateDTO dto) {
        return toAjax(imUserService.insertImUser(convertToEntity(dto)));
    }

    @RequiresPermissions("im:user:view")
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap mmap) {
        ImUser imUser = imUserService.selectImUserById(id);
        mmap.put("imUser", imUser);
        return prefix + "/edit";
    }

    @Log(title = "IM用户", businessType = BusinessType.UPDATE)
    @RequiresPermissions("im:user:edit")
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult edit(@Validated @RequestBody ImUserUpdateDTO dto) {
        ImUser imUser = convertToEntity(dto);
        return toAjax(imUserService.updateImUser(imUser));
    }

    @Log(title = "IM用户", businessType = BusinessType.DELETE)
    @RequiresPermissions("im:user:remove")
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        return toAjax(imUserService.deleteImUserByIds(ids));
    }

    private ImUser convertToEntity(ImUserCreateDTO dto) {
        ImUser imUser = new ImUser();
        BeanUtils.copyProperties(dto, imUser);
        return imUser;
    }

    private ImUser convertToEntity(ImUserUpdateDTO dto) {
        ImUser imUser = new ImUser();
        BeanUtils.copyProperties(dto, imUser);
        return imUser;
    }

    private ImUserVO convertToVO(ImUser imUser) {
        ImUserVO vo = new ImUserVO();
        BeanUtils.copyProperties(imUser, vo);
        return vo;
    }
}