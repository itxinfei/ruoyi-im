package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImUserApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 用户应用安装Mapper
 *
 * @author ruoyi
 */
@Mapper
public interface ImUserApplicationMapper extends BaseMapper<ImUserApplication> {

    /**
     * 查询用户已安装的应用列表
     *
     * @param userId 用户ID
     * @return 应用列表
     */
    @Select("SELECT * FROM im_user_application WHERE user_id = #{userId} AND is_enabled = 1 ORDER BY sort_order ASC")
    List<ImUserApplication> selectUserApplications(@Param("userId") Long userId);

    /**
     * 查询用户是否已安装指定应用
     *
     * @param userId 用户ID
     * @param appId 应用ID
     * @return 安装记录
     */
    @Select("SELECT * FROM im_user_application WHERE user_id = #{userId} AND app_id = #{appId} LIMIT 1")
    ImUserApplication selectUserApp(@Param("userId") Long userId, @Param("appId") Long appId);

    /**
     * 查询应用的安装用户数量
     *
     * @param appId 应用ID
     * @return 安装数量
     */
    @Select("SELECT COUNT(*) FROM im_user_application WHERE app_id = #{appId} AND is_enabled = 1")
    int countByAppId(@Param("appId") Long appId);

    /**
     * 更新最后使用时间
     *
     * @param userId 用户ID
     * @param appId 应用ID
     */
    @Update("UPDATE im_user_application SET last_used_time = NOW(), use_count = use_count + 1 WHERE user_id = #{userId} AND app_id = #{appId}")
    void updateLastUsedTime(@Param("userId") Long userId, @Param("appId") Long appId);

    /**
     * 增加使用次数
     *
     * @param id 记录ID
     */
    @Update("UPDATE im_user_application SET use_count = use_count + 1 WHERE id = #{id}")
    void incrementUseCount(@Param("id") Long id);
}
