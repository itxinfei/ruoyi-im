package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImUserConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户配置Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface ImUserConfigMapper extends BaseMapper<ImUserConfig> {

    /**
     * 根据用户ID和配置类型查询配置列表
     *
     * @param userId 用户ID
     * @param configType 配置类型
     * @return 配置列表
     */
    List<ImUserConfig> selectByUserIdAndType(@Param("userId") Long userId, @Param("configType") String configType);

    /**
     * 根据用户ID和配置键查询单个配置
     *
     * @param userId 用户ID
     * @param configType 配置类型
     * @param configKey 配置键
     * @return 配置对象
     */
    ImUserConfig selectByUserIdAndKey(@Param("userId") Long userId,
                                       @Param("configType") String configType,
                                       @Param("configKey") String configKey);

    /**
     * 根据用户ID删除所有配置
     *
     * @param userId 用户ID
     * @return 删除行数
     */
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 插入或更新配置
     *
     * @param config 配置对象
     * @return 影响行数
     */
    int insertOrUpdate(ImUserConfig config);
}
