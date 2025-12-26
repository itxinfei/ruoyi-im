package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * IM用户Mapper接口
 * 
 * @author ruoyi
 * @date 2024-08-09
 */
@Mapper
public interface ImUserMapper extends BaseMapper<ImUser> {

    ImUser selectImUserById(Long id);

    List<ImUser> selectImUserList(ImUser imUser);

    int insertImUser(ImUser imUser);

    int updateImUser(ImUser imUser);

    int deleteImUserByIds(Long[] ids);

    int deleteImUserById(Long id);

    ImUser selectImUserByUsername(String username);

    ImUser selectImUserByEmail(String email);

    ImUser selectImUserByPhone(String phone);

    List<ImUser> searchUsers(String keyword);

    List<ImUser> selectImUsersByIds(List<Long> userIds);

    int updateOnlineStatus(@Param("userId") Long userId, @Param("status") String status);

    String getUserOnlineStatus(Long userId);

    List<ImUser> getOnlineUsers();

    int getOnlineUserCount();

    int updateUserAvatar(@Param("userId") Long userId, @Param("avatar") String avatar);

    int updateUserNickname(@Param("userId") Long userId, @Param("nickname") String nickname);

    int updateUserSignature(@Param("userId") Long userId, @Param("signature") String signature);

    boolean checkUsernameExists(String username);

    boolean checkEmailExists(String email);

    boolean checkPhoneExists(String phone);

    List<ImUser> getRecommendUsers(@Param("userId") Long userId, @Param("limit") Integer limit);

    int updateLastActiveTime(Long userId);
}