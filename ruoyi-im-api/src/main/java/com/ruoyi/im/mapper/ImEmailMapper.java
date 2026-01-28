package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImEmail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 邮件Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface ImEmailMapper {

    /**
     * 查询邮件列表
     *
     * @param userId 用户ID
     * @param folder 文件夹
     * @return 邮件列表
     */
    List<ImEmail> selectEmailsByUserIdAndFolder(@Param("userId") Long userId, @Param("folder") String folder);

    /**
     * 查询邮件详情
     *
     * @param id 邮件ID
     * @return 邮件信息
     */
    ImEmail selectEmailById(Long id);

    /**
     * 插入邮件
     *
     * @param email 邮件信息
     * @return 影响行数
     */
    int insertEmail(ImEmail email);

    /**
     * 更新邮件
     *
     * @param email 邮件信息
     * @return 影响行数
     */
    int updateEmail(ImEmail email);

    /**
     * 删除邮件
     *
     * @param id 邮件ID
     * @return 影响行数
     */
    int deleteEmailById(Long id);

    /**
     * 获取未读邮件数量
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    int countUnreadByUserId(Long userId);

    /**
     * 批量插入邮件（用于群发）
     *
     * @param emails 邮件列表
     * @return 影响行数
     */
    int batchInsertEmails(@Param("emails") List<ImEmail> emails);

    /**
     * 搜索邮件
     *
     * @param userId 用户ID
     * @param keyword 关键词
     * @return 邮件列表
     */
    List<ImEmail> searchEmailsByKeyword(@Param("userId") Long userId, @Param("keyword") String keyword);

    /**
     * 统计各文件夹邮件数量
     *
     * @param userId 用户ID
     * @return 统计信息列表
     */
    java.util.List<java.util.Map<String, Object>> countByFolder(@Param("userId") Long userId);
}
