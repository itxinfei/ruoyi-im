package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImUrlMetadata;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * URL 元数据 Mapper 接口
 *
 * @author ruoyi
 */
@Mapper
public interface ImUrlMetadataMapper {

    /**
     * 新增 URL 元数据
     *
     * @param metadata URL 元数据
     * @return 结果
     */
    int insertImUrlMetadata(ImUrlMetadata metadata);

    /**
     * 根据 URL 查询元数据
     *
     * @param url URL 地址
     * @return URL 元数据
     */
    ImUrlMetadata selectByUrl(@Param("url") String url);

    /**
     * 更新 URL 元数据
     *
     * @param metadata URL 元数据
     * @return 结果
     */
    int updateImUrlMetadata(ImUrlMetadata metadata);

    /**
     * 删除过期的元数据
     *
     * @param beforeDate 删除此日期之前的数据
     * @return 删除数量
     */
    int deleteExpiredMetadata(@Param("beforeDate") LocalDateTime beforeDate);
}
