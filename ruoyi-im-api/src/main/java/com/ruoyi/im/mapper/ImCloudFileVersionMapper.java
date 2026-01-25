package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImCloudFileVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 云盘文件版本Mapper
 *
 * @author ruoyi
 */
@Mapper
public interface ImCloudFileVersionMapper extends BaseMapper<ImCloudFileVersion> {

    /**
     * 查询文件的所有版本
     *
     * @param cloudFileId 云盘文件ID
     * @return 版本列表
     */
    List<ImCloudFileVersion> selectByCloudFileId(@Param("cloudFileId") Long cloudFileId);

    /**
     * 查询最新版本
     *
     * @param cloudFileId 云盘文件ID
     * @return 最新版本
     */
    ImCloudFileVersion selectLatestVersion(@Param("cloudFileId") Long cloudFileId);

    /**
     * 统计文件版本数量
     *
     * @param cloudFileId 云盘文件ID
     * @return 版本数量
     */
    Integer countVersions(@Param("cloudFileId") Long cloudFileId);
}
