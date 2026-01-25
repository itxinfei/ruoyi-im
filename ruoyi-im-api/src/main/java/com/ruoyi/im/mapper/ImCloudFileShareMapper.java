package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImCloudFileShare;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 云盘文件分享Mapper
 *
 * @author ruoyi
 */
@Mapper
public interface ImCloudFileShareMapper extends BaseMapper<ImCloudFileShare> {

    /**
     * 查询用户的分享列表
     *
     * @param userId 用户ID
     * @return 分享列表
     */
    List<ImCloudFileShare> selectBySharerId(@Param("userId") Long userId);

    /**
     * 根据分享码查询
     *
     * @param shareCode 分享码
     * @return 分享记录
     */
    ImCloudFileShare selectByShareCode(@Param("shareCode") String shareCode);

    /**
     * 更新访问次数
     *
     * @param id 分享ID
     * @return 更新行数
     */
    int incrementViewCount(@Param("id") Long id);

    /**
     * 更新下载次数
     *
     * @param id 分享ID
     * @return 更新行数
     */
    int incrementDownloadCount(@Param("id") Long id);
}
