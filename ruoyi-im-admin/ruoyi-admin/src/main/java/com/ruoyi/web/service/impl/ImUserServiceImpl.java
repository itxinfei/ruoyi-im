package com.ruoyi.web.service.impl;

import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.web.domain.ImUser;
import com.ruoyi.web.mapper.ImUserMapper;
import com.ruoyi.web.service.ImUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * IM用户Service实现（Admin模块专用）
 *
 * 使用 BCryptPasswordEncoder 加密密码，无需单独存储 salt
 */
@Service
public class ImUserServiceImpl implements ImUserService {

    private static final Logger logger = LoggerFactory.getLogger(ImUserServiceImpl.class);

    @Autowired
    private ImUserMapper userMapper;

    /** BCrypt密码编码器 */
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public List<ImUser> selectImUserList(ImUser imUser) {
        return userMapper.selectImUserList(imUser);
    }

    @Override
    public ImUser selectImUserById(Long id) {
        return userMapper.selectImUserById(id);
    }

    @Override
    public List<ImUser> selectImUserByIds(Long[] ids) {
        return userMapper.selectImUserByIds(ids);
    }

    @Override
    public int insertImUser(ImUser imUser) {
        // 对密码进行加密（BCrypt）
        if (imUser.getPassword() != null && !imUser.getPassword().isEmpty()) {
            String encryptedPassword = passwordEncoder.encode(imUser.getPassword());
            imUser.setPassword(encryptedPassword);
        }
        return userMapper.insertImUser(imUser);
    }

    @Override
    public int updateImUser(ImUser imUser) {
        // 对密码进行加密（BCrypt）- 仅当密码被修改时
        if (imUser.getPassword() != null && !imUser.getPassword().isEmpty()) {
            // 检查密码是否已经是 BCrypt 格式（以 $2a$ 或 $2b$ 开头）
            if (!imUser.getPassword().startsWith("$2")) {
                String encryptedPassword = passwordEncoder.encode(imUser.getPassword());
                imUser.setPassword(encryptedPassword);
            }
        }
        return userMapper.updateImUser(imUser);
    }

    @Override
    public int deleteImUserById(Long id) {
        return userMapper.deleteImUserById(id);
    }

    @Override
    public int deleteImUserByIds(Long[] ids) {
        return userMapper.deleteImUserByIds(ids);
    }

    @Override
    public int resetPassword(Long id, String password, String adminPassword) {
        try {
            ImUser user = userMapper.selectImUserById(id);
            if (user == null) {
                return -2;
            }

            ImUser adminUser = userMapper.selectImUserById(ShiroUtils.getUserId());
            if (adminUser == null) {
                return -1;
            }

            if (!passwordEncoder.matches(adminPassword, adminUser.getPassword())) {
                return -1;
            }

            String encryptedPassword = passwordEncoder.encode(password);
            user.setPassword(encryptedPassword);
            user.setUpdateTime(new java.util.Date());

            return userMapper.updateImUser(user);
        } catch (Exception e) {
            logger.error("重置用户密码失败: userId={}", id, e);
            return 0;
        }
    }

    @Override
    public int changeStatus(Long id, String status) {
        return userMapper.changeStatus(id, status);
    }

    @Override
    public int countOnlineUsers() {
        return userMapper.countOnlineUsers();
    }

    @Override
    public boolean checkUsernameUnique(String username) {
        return userMapper.checkUsernameUnique(username);
    }

    @Override
    public List<ImUser> searchUsers(String keyword) {
        return userMapper.searchUsers(keyword);
    }

    @Override
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> result = new HashMap<>();
        int total = userMapper.countTotalUsers();
        int online = userMapper.countOnlineUsers();
        result.put("totalCount", total);
        result.put("onlineCount", online);
        result.put("offlineCount", total - online);
        result.put("disabledCount", userMapper.countDisabledUsers());
        return result;
    }

    @Override
    public int batchUpdateStatus(Long[] userIds, String status) {
        if (userIds == null || userIds.length == 0) {
            return 0;
        }

        int count = 0;
        for (Long userId : userIds) {
            if (userId != null && userId > 0) {
                count += changeStatus(userId, status);
            }
        }
        return count;
    }

    /**
     * 手机号正则表达式
     */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    /**
     * 邮箱正则表达式
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @Override
    @Transactional
    public Map<String, Object> batchImportUsers(List<ImUser> users, boolean updateSupported) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;
        List<Map<String, String>> failList = new ArrayList<>();

        if (users == null || users.isEmpty()) {
            result.put("totalRows", 0);
            result.put("successCount", 0);
            result.put("failCount", 0);
            result.put("failList", failList);
            return result;
        }

        for (int i = 0; i < users.size(); i++) {
            ImUser user = users.get(i);
            int rowNum = i + 2; // Excel 行号从2开始（第1行是表头）

            try {
                // 数据校验
                String errorMsg = validateUser(user);
                if (errorMsg != null) {
                    failCount++;
                    Map<String, String> failInfo = new HashMap<>();
                    failInfo.put("row", String.valueOf(rowNum));
                    failInfo.put("username", user.getUsername() != null ? user.getUsername() : "");
                    failInfo.put("reason", errorMsg);
                    failList.add(failInfo);
                    logger.warn("导入用户失败（第{}行）: {}", rowNum, errorMsg);
                    continue;
                }

                // 检查用户名是否已存在
                boolean isExist = !checkUsernameUnique(user.getUsername());
                ImUser existUser = null;
                if (isExist) {
                    // 获取已存在的用户
                    List<ImUser> existList = userMapper.selectImUserList(
                        new ImUser() {{ setUsername(user.getUsername()); }}
                    );
                    if (!existList.isEmpty()) {
                        existUser = existList.get(0);
                    }
                }

                if (isExist) {
                    if (updateSupported && existUser != null) {
                        // 更新已存在的用户
                        user.setId(existUser.getId());
                        // 密码为空则保留原密码
                        if (user.getPassword() == null || user.getPassword().isEmpty()) {
                            user.setPassword(null);
                        }
                        updateImUser(user);
                        successCount++;
                        logger.info("更新用户成功（第{}行）: {}", rowNum, user.getUsername());
                    } else {
                        failCount++;
                        Map<String, String> failInfo = new HashMap<>();
                        failInfo.put("row", String.valueOf(rowNum));
                        failInfo.put("username", user.getUsername());
                        failInfo.put("reason", "用户名已存在");
                        failList.add(failInfo);
                        logger.warn("导入用户失败（第{}行）: 用户名已存在 - {}", rowNum, user.getUsername());
                    }
                } else {
                    // 新增用户
                    // 设置默认密码为 123456
                    if (user.getPassword() == null || user.getPassword().isEmpty()) {
                        user.setPassword("123456");
                    }
                    // 设置默认状态 (Integer类型: 0=正常, 1=禁用)
                    if (user.getStatus() == null) {
                        user.setStatus(0); // 0=正常
                    }
                    insertImUser(user);
                    successCount++;
                    logger.info("导入用户成功（第{}行）: {}", rowNum, user.getUsername());
                }

            } catch (Exception e) {
                failCount++;
                Map<String, String> failInfo = new HashMap<>();
                failInfo.put("row", String.valueOf(rowNum));
                failInfo.put("username", user.getUsername() != null ? user.getUsername() : "");
                failInfo.put("reason", "系统异常: " + e.getMessage());
                failList.add(failInfo);
                logger.error("导入用户失败（第{}行）: {}", rowNum, e.getMessage(), e);
            }
        }

        result.put("totalRows", users.size());
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("failList", failList);
        logger.info("批量导入用户完成 - 总行数: {}, 成功: {}, 失败: {}",
            users.size(), successCount, failCount);

        return result;
    }

    /**
     * 校验用户数据
     *
     * @param user 用户对象
     * @return 错误信息，null 表示校验通过
     */
    private String validateUser(ImUser user) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return "用户名不能为空";
        }
        if (user.getUsername().length() > 30) {
            return "用户名长度不能超过30个字符";
        }

        // 昵称非必填，但如果有则限制长度
        if (user.getNickname() != null && user.getNickname().length() > 50) {
            return "昵称长度不能超过50个字符";
        }

        // 校验手机号
        if (user.getMobile() != null && !user.getMobile().trim().isEmpty()) {
            if (!PHONE_PATTERN.matcher(user.getMobile()).matches()) {
                return "手机号格式不正确";
            }
            // 检查手机号是否已存在
            List<ImUser> existList = userMapper.selectImUserList(
                new ImUser() {{ setMobile(user.getMobile()); }}
            );
            for (ImUser existUser : existList) {
                if (!existUser.getUsername().equals(user.getUsername())) {
                    return "手机号已被其他用户使用";
                }
            }
        }

        // 校验邮箱
        if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            if (!EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
                return "邮箱格式不正确";
            }
            // 检查邮箱是否已存在
            List<ImUser> existList = userMapper.selectImUserList(
                new ImUser() {{ setEmail(user.getEmail()); }}
            );
            for (ImUser existUser : existList) {
                if (!existUser.getUsername().equals(user.getUsername())) {
                    return "邮箱已被其他用户使用";
                }
            }
        }

        return null;
    }
}
