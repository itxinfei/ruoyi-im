package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImMessageEditHistory;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 消息编辑历史Mapper
 */
public interface ImMessageEditHistoryMapper extends BaseMapper<ImMessageEditHistory> {

    /**
     * 查询消息的编辑历史列表（带用户信息）
     */
    @Select("SELECT h.*, u.nickname as editorName, u.avatar as editorAvatar " +
            "FROM im_message_edit_history h " +
            "LEFT JOIN im_user u ON h.editor_id = u.id " +
            "WHERE h.message_id = #{messageId} " +
            "ORDER BY h.edit_time DESC")
    List<ImMessageEditHistory> selectByMessageId(@Param("messageId") Long messageId);

    /**
     * 统计消息的编辑次数
     */
    @Select("SELECT COUNT(*) FROM im_message_edit_history WHERE message_id = #{messageId}")
    Integer countByMessageId(@Param("messageId") Long messageId);
}
