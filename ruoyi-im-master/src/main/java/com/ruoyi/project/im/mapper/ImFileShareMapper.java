package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.ImFileShare;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

/**
 * IM文件分享数据访问层接口（Admin模块专用）
 *
 * <p>负责处理IM文件分享管理相关的数据库操作</p>
 * <p>主要功能包括：文件分享的增删改查、访问次数统计、下载次数统计、过期分享清理等</p>
 *
 * @author ruoyi
 * @date 2025-01-07
 */
@Mapper
public interface ImFileShareMapper {


    /**
     * 查询文件分享
     *
     * @param id 文件分享主键
     * @return 文件分享
     */
    ImFileShare selectImFileShareById(Long id);

    /**
     * 查询文件分享列表
     *
     * @param imFileShare 文件分享
     * @return 文件分享集合
     */
    List<ImFileShare> selectImFileShareList(ImFileShare imFileShare);

    /**
     * 查询用户的分享列表
     *
     * @param sharerId 分享者ID
     * @return 分享列表
     */
    List<ImFileShare> selectSharesBySharerId(Long sharerId);

    /**
     * 新增文件分享
     *
     * @param imFileShare 文件分享
     * @return 结果
     */
    int insertImFileShare(ImFileShare imFileShare);

    /**
     * 修改文件分享
     *
     * @param imFileShare 文件分享
     * @return 结果
     */
    int updateImFileShare(ImFileShare imFileShare);

    /**
     * 删除文件分享
     *
     * @param id 文件分享主键
     * @return 结果
     */
    int deleteImFileShareById(Long id);

    /**
     * 批量删除文件分享
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteImFileShareByIds(Long[] ids);

    /**
     * 增加访问次数
     *
     * @param id 分享ID
     * @return 结果
     */
    int incrementAccessCount(Long id);

    /**
     * 增加下载次数
     *
     * @param id 分享ID
     * @return 结果
     */
    int incrementDownloadCount(Long id);

    /**
     * 获取文件分享统计数据
     *
     * @return 统计数据
     */
    Map<String, Object> getFileShareStatistics();

    /**
     * 查询已过期的分享
     *
     * @return 分享列表
     */
    List<ImFileShare> selectExpiredShares();

    /**
     * 清理过期分享
     *
     * @return 清理数量
     */
    int cleanExpiredShares();
}
