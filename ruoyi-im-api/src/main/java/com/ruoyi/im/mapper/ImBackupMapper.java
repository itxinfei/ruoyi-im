package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImBackup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 数据备份Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface ImBackupMapper extends BaseMapper<ImBackup> {

    /**
     * 查询所有备份记录（按创建时间倒序）
     *
     * @return 备份记录列表
     */
    List<ImBackup> selectAllOrderByCreateTime();

    /**
     * 根据状态查询备份记录
     *
     * @param status 状态
     * @return 备份记录列表
     */
    List<ImBackup> selectByStatus(@Param("status") String status);

    /**
     * 根据备份类型查询备份记录
     *
     * @param backupType 备份类型
     * @return 备份记录列表
     */
    List<ImBackup> selectByBackupType(@Param("backupType") String backupType);

    /**
     * 更新备份状态
     *
     * @param id 备份ID
     * @param status 状态
     * @param errorMessage 错误信息
     * @return 影响行数
     */
    int updateStatus(@Param("id") Long id,
                     @Param("status") String status,
                     @Param("errorMessage") String errorMessage);

    /**
     * 更新备份完成信息
     *
     * @param id 备份ID
     * @param filePath 文件路径
     * @param fileSize 文件大小
     * @return 影响行数
     */
    int updateCompleteInfo(@Param("id") Long id,
                           @Param("filePath") String filePath,
                           @Param("fileSize") Long fileSize);
}
