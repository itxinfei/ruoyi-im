package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImNudge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 拍一拍Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface ImNudgeMapper extends BaseMapper<ImNudge> {

    /**
     * 查询两个用户之间最近的拍一拍记录
     *
     * @param nudgerId   发起者ID
     * @param nudgedUserId 被拍用户ID
     * @param sinceTime   起始时间（用于判断冷却时间）
     * @return 最近的拍一拍记录列表
     */
    @Select("SELECT * FROM im_nudge " +
            "WHERE nudger_id = #{nudgerId} " +
            "AND nudged_user_id = #{nudgedUserId} " +
            "AND create_time >= #{sinceTime} " +
            "ORDER BY create_time DESC " +
            "LIMIT 1")
    ImNudge findRecentNudge(
            @Param("nudgerId") Long nudgerId,
            @Param("nudgedUserId") Long nudgedUserId,
            @Param("sinceTime") LocalDateTime sinceTime
    );

    /**
     * 查询会话中的拍一拍记录列表
     *
     * @param conversationId 会话ID
     * @param limit          限制数量
     * @return 拍一拍记录列表
     */
    @Select("SELECT * FROM im_nudge " +
            "WHERE conversation_id = #{conversationId} " +
            "ORDER BY create_time DESC " +
            "LIMIT #{limit}")
    List<ImNudge> selectByConversationId(
            @Param("conversationId") Long conversationId,
            @Param("limit") int limit
    );
}
