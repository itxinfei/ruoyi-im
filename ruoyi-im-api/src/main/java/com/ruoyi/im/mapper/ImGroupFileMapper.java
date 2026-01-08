package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.im.domain.ImGroupFile;
import com.ruoyi.im.dto.group.ImGroupFileQueryRequest;
import com.ruoyi.im.vo.group.ImGroupFileStatisticsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 群组文件Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface ImGroupFileMapper extends BaseMapper<ImGroupFile> {

    /**
     * 分页查询群组文件
     *
     * @param page 分页对象
     * @param request 查询条件
     * @return 文件列表
     */
    IPage<ImGroupFile> selectFilePage(Page<ImGroupFile> page, @Param("req") ImGroupFileQueryRequest request);

    /**
     * 根据群组ID统计文件信息
     *
     * @param groupId 群组ID
     * @return 统计信息
     */
    ImGroupFileStatisticsVO selectStatisticsByGroupId(@Param("groupId") Long groupId);

    /**
     * 查询群组文件分类列表
     *
     * @param groupId 群组ID
     * @return 分类列表
     */
    List<String> selectCategoriesByGroupId(@Param("groupId") Long groupId);

    /**
     * 更新下载次数
     *
     * @param fileId 文件ID
     * @return 更新行数
     */
    int incrementDownloadCount(@Param("fileId") Long fileId);
}
