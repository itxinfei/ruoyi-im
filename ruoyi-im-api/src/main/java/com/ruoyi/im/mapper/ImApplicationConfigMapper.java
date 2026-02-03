package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImApplicationConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 应用配置Mapper
 *
 * @author ruoyi
 */
@Mapper
public interface ImApplicationConfigMapper extends BaseMapper<ImApplicationConfig> {

    /**
     * 查询应用的所有配置项
     *
     * @param appId 应用ID
     * @return 配置项列表
     */
    @Select("SELECT * FROM im_application_config WHERE app_id = #{appId} AND is_enabled = 1 ORDER BY sort_order ASC")
    List<ImApplicationConfig> selectByAppId(@Param("appId") Long appId);

    /**
     * 查询指定配置键的配置项
     *
     * @param appId 应用ID
     * @param configKey 配置键
     * @return 配置项
     */
    @Select("SELECT * FROM im_application_config WHERE app_id = #{appId} AND config_key = #{configKey} LIMIT 1")
    ImApplicationConfig selectByAppIdAndKey(@Param("appId") Long appId, @Param("configKey") String configKey);

    /**
     * 查询指定分组的配置项
     *
     * @param appId 应用ID
     * @param configGroup 配置分组
     * @return 配置项列表
     */
    @Select("SELECT * FROM im_application_config WHERE app_id = #{appId} AND config_group = #{configGroup} AND is_enabled = 1 ORDER BY sort_order ASC")
    List<ImApplicationConfig> selectByAppIdAndGroup(@Param("appId") Long appId, @Param("configGroup") String configGroup);

    /**
     * 删除应用的所有配置
     *
     * @param appId 应用ID
     */
    @Select("DELETE FROM im_application_config WHERE app_id = #{appId}")
    void deleteByAppId(@Param("appId") Long appId);
}
