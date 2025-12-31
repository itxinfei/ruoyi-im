package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImUser;
import java.util.List;

/**
 * IM用户Mapper接口
 * 
 * @author ruoyi
 */
public interface ImUserMapper {
    /**
     * 查询IM用户
     * 
     * @param id 用户ID
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
     * 删除IM用户
     * 
     * @param id 用户ID
     * @return 结果
     */
    public int deleteImUserById(Long id);

    /**
     * 批量删除IM用户
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteImUserByIds(Long[] ids);
    
    /**
     * 根据用户名查询用户
     * 
     * @param username 用户名
     * @return IM用户
     */
    public ImUser selectImUserByUsername(String username);
    
    /**
     * 根据邮箱查询用户
     * 
     * @param email 邮箱
     * @return IM用户
     */
    public ImUser selectImUserByEmail(String email);
}