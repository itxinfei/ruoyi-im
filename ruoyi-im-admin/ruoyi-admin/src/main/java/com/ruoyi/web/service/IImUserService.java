package com.ruoyi.web.service;

import com.ruoyi.system.domain.ImUser;
import java.util.List;

/**
 * IM用户Service接口
 * 
 * @author ruoyi
 */
public interface IImUserService 
{
    /**
     * 查询IM用户
     * 
     * @param id IM用户ID
     * @return IM用户
     */
    public ImUser selectImUserById(Long id);

    /**
     * 查询IM用户列表
     * 
     * @param imUser IM用户
     * @return IM用户集合
     */
    public List<ImUser> selectImUserList(ImUser imUser);

    /**
     * 新增IM用户
     * 
     * @param imUser IM用户
     * @return 结果
     */
    public int insertImUser(ImUser imUser);

    /**
     * 修改IM用户
     * 
     * @param imUser IM用户
     * @return 结果
     */
    public int updateImUser(ImUser imUser);

    /**
     * 批量删除IM用户
     * 
     * @param ids 需要删除的IM用户ID
     * @return 结果
     */
    public int deleteImUserByIds(Long[] ids);

    /**
     * 删除IM用户信息
     * 
     * @param id IM用户ID
     * @return 结果
     */
    public int deleteImUserById(Long id);
}