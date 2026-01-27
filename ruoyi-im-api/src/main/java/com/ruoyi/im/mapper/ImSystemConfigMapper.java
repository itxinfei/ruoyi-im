package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImSystemConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 系统配置Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface ImSystemConfigMapper extends BaseMapper<ImSystemConfig> {

    @Select("SELECT * FROM im_system_config")
    List<ImSystemConfig> selectAllConfigs();

    @Select("SELECT * FROM im_system_config WHERE config_key = #{key} LIMIT 1")
    ImSystemConfig selectByKey(String key);
}
