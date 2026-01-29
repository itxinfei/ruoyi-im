package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImQuickReply;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 快捷回复Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface ImQuickReplyMapper extends BaseMapper<ImQuickReply> {

    /**
     * 查询用户的快捷回复列表
     *
     * @param userId 用户ID
     * @return 快捷回复列表
     */
    List<ImQuickReply> selectUserReplies(@Param("userId") Long userId);

    /**
     * 查询用户指定分类的快捷回复
     *
     * @param userId 用户ID
     * @param category 分类
     * @return 快捷回复列表
     */
    List<ImQuickReply> selectByCategory(@Param("userId") Long userId, @Param("category") String category);

    /**
     * 查询系统默认快捷回复
     *
     * @return 快捷回复列表
     */
    List<ImQuickReply> selectSystemReplies();

    /**
     * 增加使用次数
     *
     * @param id 快捷回复ID
     * @return 更新行数
     */
    int incrementUseCount(@Param("id") Long id);
}
