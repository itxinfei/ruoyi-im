package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImUserLayout;
import org.apache.ibatis.annotations.Param;

/**
 * 用户布局配置Mapper接口
 *
 * @author ruoyi
 */
public interface ImUserLayoutMapper {

    /**
     * 查询用户的布局配置
     *
     * @param userId 用户ID
     * @param layoutType 布局类型
     * @return 布局配置
     */
    ImUserLayout selectByUserIdAndType(@Param("userId") Long userId, @Param("layoutType") String layoutType);

    /**
     * 新增或更新布局配置
     *
     * @param userLayout 布局配置
     * @return 影响行数
     */
    int insertOrUpdate(ImUserLayout userLayout);

    /**
     * 删除用户的布局配置
     *
     * @param userId 用户ID
     * @param layoutType 布局类型
     * @return 影响行数
     */
    int deleteByUserIdAndType(@Param("userId") Long userId, @Param("layoutType") String layoutType);
}
