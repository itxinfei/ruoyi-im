package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImAppUsage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 应用使用记录Mapper接口
 *
 * @author ruoyi
 */
public interface ImAppUsageMapper {

    /**
     * 新增应用使用记录
     *
     * @param appUsage 使用记录
     * @return 影响行数
     */
    int insertImAppUsage(ImAppUsage appUsage);

    /**
     * 查询用户最近使用的应用ID列表
     *
     * @param userId 用户ID
     * @param limit 返回数量限制
     * @return 最近使用的应用ID列表（按使用时间倒序）
     */
    List<Long> selectRecentAppIdsByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 删除用户指定应用的使用记录
     *
     * @param userId 用户ID
     * @param appId 应用ID
     * @return 影响行数
     */
    int deleteByUserIdAndAppId(@Param("userId") Long userId, @Param("appId") Long appId);

    /**
     * 清理用户旧的使用记录（保留最近的N条）
     *
     * @param userId 用户ID
     * @param keepCount 保留数量
     * @return 影响行数
     */
    int deleteOldRecordsByUserId(@Param("userId") Long userId, @Param("keepCount") int keepCount);
}
