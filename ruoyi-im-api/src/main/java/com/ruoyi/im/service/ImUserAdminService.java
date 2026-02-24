package com.ruoyi.im.service;

import com.ruoyi.im.dto.BasePageRequest;
import com.ruoyi.im.dto.user.ImRegisterRequest;
import com.ruoyi.im.vo.user.ImUserVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 用户管理服务接口（管理员操作）
 */
public interface ImUserAdminService {

    List<ImUserVO> getUserList(BasePageRequest request);

    Long createUser(ImRegisterRequest request);

    void deleteUser(Long userId);

    void batchDeleteUsers(List<Long> userIds);

    void resetPassword(Long userId, String newPassword);

    List<ImUserVO> getUserListWithPagination(String keyword, String role, int offset, int limit);

    int countUsers(String keyword, String role);

    void updateRole(Long userId, String role);

    Map<String, Long> getUserStats();

    Long adminCreateUser(Map<String, Object> data);

    void adminUpdateUser(Long userId, Map<String, Object> data);

    void batchUpdateUserStatus(List<Long> userIds, Integer status);

    Map<String, Object> importUsers(MultipartFile file);

    void downloadTemplate(HttpServletResponse response) throws IOException;

    void exportUsers(HttpServletResponse response, String keyword, String role) throws IOException;
}
