package com.ruoyi.im.util;

import com.ruoyi.im.constant.ImErrorCode;
import com.ruoyi.im.exception.BusinessException;

/**
 * 业务异常辅助工具类
 * 用于统一抛出BusinessException，减少重复代码
 *
 * @author ruoyi
 */
public final class BusinessExceptionHelper {

    private BusinessExceptionHelper() {
        // 工具类禁止实例化
    }

    // ========== 通用错误抛出方法 ==========

    /**
     * 抛出资源不存在异常
     */
    public static void throwNotFound(String resourceName) {
        throw new BusinessException(ImErrorCode.NOT_EXIST, resourceName + "不存在");
    }

    /**
     * 抛出资源不存在异常（带详细消息）
     */
    public static void throwNotFound(String resourceName, String detail) {
        throw new BusinessException(ImErrorCode.NOT_EXIST, resourceName + "不存在: " + detail);
    }

    /**
     * 抛出会议不存在异常
     */
    public static void throwMeetingNotFound() {
        throw new BusinessException(ImErrorCode.NOT_EXIST, "会议不存在");
    }

    /**
     * 抛出参与者不在会议中异常
     */
    public static void throwParticipantNotInMeeting() {
        throw new BusinessException(ImErrorCode.NOT_EXIST, "参与者不在会议中");
    }

    /**
     * 抛出名称已存在异常
     */
    public static void throwNameExists(String resourceName) {
        throw new BusinessException(ImErrorCode.NAME_EXISTS, resourceName + "名称已存在");
    }

    /**
     * 抛出编号已存在异常
     */
    public static void throwCodeExists(String resourceName) {
        throw new BusinessException(ImErrorCode.CODE_EXISTS, resourceName + "编号已存在");
    }

    /**
     * 抛出无权限异常
     */
    public static void throwNoPermission() {
        throw new BusinessException(ImErrorCode.NO_PERMISSION, "无权限操作");
    }

    /**
     * 抛出无权限异常（带详细消息）
     */
    public static void throwNoPermission(String message) {
        throw new BusinessException(ImErrorCode.NO_PERMISSION, message);
    }

    /**
     * 抛出状态不正确异常
     */
    public static void throwStatusError(String resourceName) {
        throw new BusinessException(ImErrorCode.STATUS_ERROR, resourceName + "状态不正确");
    }

    /**
     * 抛出已存在异常
     */
    public static void throwAlreadyExists(String resourceName) {
        throw new BusinessException(ImErrorCode.ALREADY_EXISTS, resourceName + "已存在");
    }

    /**
     * 抛出不允许操作异常
     */
    public static void throwNotAllowed(String message) {
        throw new BusinessException(ImErrorCode.NOT_ALLOWED, message);
    }

    /**
     * 抛出已被使用异常
     */
    public static void throwInUse(String resourceName) {
        throw new BusinessException(ImErrorCode.IN_USE, resourceName + "已被使用");
    }

    // ========== 文件相关错误抛出方法 ==========

    /**
     * 抛出文件不存在异常
     */
    public static void throwFileNotFound() {
        throw new BusinessException(ImErrorCode.FILE_NOT_FOUND, "文件不存在");
    }

    /**
     * 抛出文件上传失败异常
     */
    public static void throwFileUploadFailed(String detail) {
        throw new BusinessException(ImErrorCode.FILE_UPLOAD_FAILED, "文件上传失败: " + detail);
    }

    // ========== 部门相关错误抛出方法 ==========

    /**
     * 抛出部门不存在异常
     */
    public static void throwDepartmentNotFound() {
        throw new BusinessException(ImErrorCode.DEPARTMENT_NOT_EXIST, "部门不存在");
    }

    /**
     * 抛出部门名称已存在异常
     */
    public static void throwDepartmentNameExists() {
        throw new BusinessException(ImErrorCode.DEPARTMENT_NAME_EXISTS, "部门名称已存在");
    }

    // ========== 会议室相关错误抛出方法 ==========

    /**
     * 抛出会议室不存在异常
     */
    public static void throwMeetingRoomNotFound() {
        throw new BusinessException(ImErrorCode.MEETING_ROOM_NOT_EXIST, "会议室不存在");
    }

    /**
     * 抛出会议室编号已存在异常
     */
    public static void throwMeetingRoomCodeExists() {
        throw new BusinessException(ImErrorCode.MEETING_ROOM_CODE_EXISTS, "会议室编号已存在");
    }

    /**
     * 抛出会议室不可预订异常
     */
    public static void throwMeetingRoomNotAvailable() {
        throw new BusinessException(ImErrorCode.MEETING_ROOM_NOT_AVAILABLE, "会议室不可预订");
    }

    /**
     * 抛出时间段已被预订异常
     */
    public static void throwTimeSlotBooked() {
        throw new BusinessException(ImErrorCode.TIME_SLOT_BOOKED, "时间段已被预订");
    }

    // ========== 考勤相关错误抛出方法 ==========

    /**
     * 抛出考勤组不存在异常
     */
    public static void throwAttendanceGroupNotFound() {
        throw new BusinessException(ImErrorCode.ATTENDANCE_GROUP_NOT_EXIST, "考勤组不存在");
    }

    /**
     * 抛出班次不存在异常
     */
    public static void throwShiftNotFound() {
        throw new BusinessException(ImErrorCode.SHIFT_NOT_EXIST, "班次不存在");
    }

    /**
     * 抛出打卡记录不存在异常
     */
    public static void throwAttendanceNotFound() {
        throw new BusinessException(ImErrorCode.ATTENDANCE_NOT_EXIST, "打卡记录不存在");
    }

    // ========== 文档相关错误抛出方法 ==========

    /**
     * 抛出文档不存在异常
     */
    public static void throwDocumentNotFound() {
        throw new BusinessException(ImErrorCode.DOCUMENT_NOT_EXIST, "文档不存在");
    }

    /**
     * 抛出文档无权限操作异常
     */
    public static void throwDocumentNoPermission() {
        throw new BusinessException(ImErrorCode.DOCUMENT_NO_PERMISSION, "无权限操作此文档");
    }

    // ========== 任务相关错误抛出方法 ==========

    /**
     * 抛出任务不存在异常
     */
    public static void throwTaskNotFound() {
        throw new BusinessException(ImErrorCode.TASK_NOT_EXIST, "任务不存在");
    }

    /**
     * 抛出评论不存在异常
     */
    public static void throwCommentNotFound() {
        throw new BusinessException(ImErrorCode.COMMENT_NOT_EXIST, "评论不存在");
    }

    /**
     * 抛出附件不存在异常
     */
    public static void throwAttachmentNotFound() {
        throw new BusinessException(ImErrorCode.ATTACHMENT_NOT_EXIST, "附件不存在");
    }

    // ========== 审批相关错误抛出方法 ==========

    /**
     * 抛出审批不存在异常
     */
    public static void throwApprovalNotFound() {
        throw new BusinessException(ImErrorCode.APPROVAL_NOT_EXIST, "审批不存在");
    }

    /**
     * 抛出模板不存在异常
     */
    public static void throwTemplateNotFound() {
        throw new BusinessException(ImErrorCode.TEMPLATE_NOT_EXIST, "模板不存在");
    }

    // ========== 群组相关错误抛出方法 ==========

    /**
     * 抛出群组不存在异常
     */
    public static void throwGroupNotFound() {
        throw new BusinessException(ImErrorCode.GROUP_NOT_EXIST, "群组不存在");
    }

    /**
     * 抛出群成员不存在异常
     */
    public static void throwGroupMemberNotFound() {
        throw new BusinessException(ImErrorCode.GROUP_MEMBER_NOT_EXIST, "群成员不存在");
    }

    /**
     * 抛出不能在群组中异常
     */
    public static void throwNotInGroup() {
        throw new BusinessException(ImErrorCode.NOT_IN_GROUP, "您不在该群组中");
    }

    /**
     * 抛出只有群主可以操作异常
     */
    public static void throwOwnerOnly() {
        throw new BusinessException(ImErrorCode.OWNER_ONLY, "只有群主可以操作");
    }

    /**
     * 抛出不能移除群主异常
     */
    public static void throwCannotRemoveOwner() {
        throw new BusinessException(ImErrorCode.CANNOT_REMOVE_OWNER, "不能移除群主");
    }

    // ========== 好友相关错误抛出方法 ==========

    /**
     * 抛出好友关系不存在异常
     */
    public static void throwFriendNotFound() {
        throw new BusinessException(ImErrorCode.FRIEND_NOT_EXIST, "好友关系不存在");
    }

    /**
     * 抛出好友申请不存在异常
     */
    public static void throwFriendRequestNotFound() {
        throw new BusinessException(ImErrorCode.FRIEND_REQUEST_NOT_EXIST, "好友申请不存在");
    }

    /**
     * 抛出不能添加自己为好友异常
     */
    public static void throwCannotAddSelf() {
        throw new BusinessException(ImErrorCode.CANNOT_ADD_SELF, "不能添加自己为好友");
    }

    /**
     * 抛出已经是好友异常
     */
    public static void throwAlreadyFriends() {
        throw new BusinessException(ImErrorCode.ALREADY_FRIENDS, "已经是好友关系");
    }

    // ========== 会话相关错误抛出方法 ==========

    /**
     * 抛出会话不存在异常
     */
    public static void throwConversationNotFound() {
        throw new BusinessException(ImErrorCode.CONVERSATION_NOT_FOUND, "会话不存在");
    }

    /**
     * 抛出会话成员不存在异常
     */
    public static void throwConversationMemberNotFound() {
        throw new BusinessException(ImErrorCode.CONVERSATION_MEMBER_NOT_FOUND, "会话成员不存在");
    }

    // ========== 云盘相关错误抛出方法 ==========

    /**
     * 抛出文件夹不存在异常
     */
    public static void throwFolderNotFound() {
        throw new BusinessException(ImErrorCode.FOLDER_NOT_EXIST, "文件夹不存在");
    }

    /**
     * 抛出云文件不存在异常
     */
    public static void throwCloudFileNotFound() {
        throw new BusinessException(ImErrorCode.CLOUD_FILE_NOT_EXIST, "文件不存在");
    }

    /**
     * 抛出存储空间不足异常
     */
    public static void throwStorageInsufficient() {
        throw new BusinessException(ImErrorCode.STORAGE_INSUFFICIENT, "存储空间不足");
    }

    /**
     * 抛出分享不存在异常
     */
    public static void throwShareNotFound() {
        throw new BusinessException(ImErrorCode.SHARE_NOT_EXIST, "分享不存在");
    }

    /**
     * 抛出分享已取消异常
     */
    public static void throwShareCancelled() {
        throw new BusinessException(ImErrorCode.SHARE_CANCELLED, "分享已取消");
    }

    /**
     * 抛出分享已过期异常
     */
    public static void throwShareExpired() {
        throw new BusinessException(ImErrorCode.SHARE_EXPIRED, "分享已过期");
    }

    /**
     * 抛出云文件不在回收站异常
     */
    public static void throwCloudFileNotInTrash() {
        throw new BusinessException(ImErrorCode.CLOUD_FILE_NOT_IN_TRASH, "文件不在回收站");
    }

    /**
     * 抛出文件夹不在回收站异常
     */
    public static void throwFolderNotInTrash() {
        throw new BusinessException(ImErrorCode.FOLDER_NOT_IN_TRASH, "文件夹不在回收站");
    }

    /**
     * 抛出密码错误异常
     */
    public static void throwPasswordError() {
        throw new BusinessException(ImErrorCode.PASSWORD_ERROR, "密码错误");
    }

    // ========== 日程相关错误抛出方法 ==========

    /**
     * 抛出日程不存在异常
     */
    public static void throwScheduleEventNotFound() {
        throw new BusinessException(ImErrorCode.SCHEDULE_NOT_EXIST, "日程不存在");
    }

    // ========== 待办相关错误抛出方法 ==========

    /**
     * 抛出待办事项不存在异常
     */
    public static void throwTodoItemNotFound() {
        throw new BusinessException(ImErrorCode.TODO_NOT_EXIST, "待办事项不存在");
    }

    // ========== 工作日志相关错误抛出方法 ==========

    /**
     * 抛出工作日志不存在异常
     */
    public static void throwWorkReportNotFound() {
        throw new BusinessException(ImErrorCode.WORK_REPORT_NOT_EXIST, "工作日志不存在");
    }

    // ========== DING消息相关错误抛出方法 ==========

    /**
     * 抛出DING消息不存在异常
     */
    public static void throwDingMessageNotFound() {
        throw new BusinessException(ImErrorCode.DING_NOT_FOUND, "DING消息不存在");
    }

    // ========== 语音转文字相关错误抛出方法 ==========

    /**
     * 抛出语音转文字功能未启用异常
     */
    public static void throwVoiceTranscriptDisabled() {
        throw new BusinessException(ImErrorCode.VOICE_TRANSCRIPT_DISABLED, "语音转文字功能未启用");
    }

    // ========== 视频通话相关错误抛出方法 ==========

    /**
     * 抛出通话不存在异常
     */
    public static void throwVideoCallNotFound() {
        throw new BusinessException(ImErrorCode.VIDEO_CALL_NOT_EXIST, "通话不存在或已过期");
    }

    /**
     * 抛出通话状态不正确异常
     */
    public static void throwVideoCallStatusError() {
        throw new BusinessException(ImErrorCode.VIDEO_CALL_STATUS_ERROR, "通话状态不正确");
    }

    // ========== 会议相关错误抛出方法 ==========

    /**
     * 抛出会议不存在异常
     */
    public static void throwVideoMeetingNotFound() {
        throw new BusinessException(ImErrorCode.VIDEO_MEETING_NOT_EXIST, "会议不存在");
    }

    /**
     * 抛出会议状态不正确异常
     */
    public static void throwVideoMeetingStatusError() {
        throw new BusinessException(ImErrorCode.VIDEO_MEETING_STATUS_ERROR, "会议状态不正确");
    }

    // ========== 外部联系人相关错误抛出方法 ==========

    /**
     * 抛出分组不存在异常
     */
    public static void throwContactGroupNotFound() {
        throw new BusinessException(ImErrorCode.CONTACT_GROUP_NOT_EXIST, "分组不存在");
    }

    /**
     * 抛出联系人不存在异常
     */
    public static void throwExternalContactNotFound() {
        throw new BusinessException(ImErrorCode.EXTERNAL_CONTACT_NOT_EXIST, "联系人不存在");
    }

    // ========== 文件上传相关错误抛出方法 ==========

    /**
     * 抛出上传任务不存在异常
     */
    public static void throwUploadTaskNotFound() {
        throw new BusinessException(ImErrorCode.UPLOAD_TASK_NOT_EXIST, "上传任务不存在");
    }

    /**
     * 抛出分片信息不存在异常
     */
    public static void throwChunkNotFound() {
        throw new BusinessException(ImErrorCode.CHUNK_NOT_EXIST, "分片信息不存在");
    }

    // ========== 收藏相关错误抛出方法 ==========

    /**
     * 抛出收藏记录不存在异常
     */
    public static void throwFavoriteNotFound() {
        throw new BusinessException(ImErrorCode.FAVORITE_NOT_EXIST, "收藏记录不存在");
    }

    /**
     * 抛出消息已收藏异常
     */
    public static void throwAlreadyFavorited() {
        throw new BusinessException(ImErrorCode.ALREADY_FAVORITED, "该消息已收藏");
    }

    // ========== 标记相关错误抛出方法 ==========

    /**
     * 抛出标记不存在异常
     */
    public static void throwMarkerNotFound() {
        throw new BusinessException(ImErrorCode.MARKER_NOT_EXIST, "标记不存在");
    }

    // ========== 反应相关错误抛出方法 ==========

    /**
     * 抛出反应不存在异常
     */
    public static void throwReactionNotFound() {
        throw new BusinessException(ImErrorCode.REACTION_NOT_EXIST, "反应不存在或已删除");
    }

    // ========== 系统通知相关错误抛出方法 ==========

    /**
     * 抛出通知不存在异常
     */
    public static void throwNotificationNotFound() {
        throw new BusinessException(ImErrorCode.NOTIFICATION_NOT_EXIST, "通知不存在");
    }

    // ========== 协作相关错误抛出方法 ==========

    /**
     * 抛出协作者不存在异常
     */
    public static void throwCollaboratorNotFound() {
        throw new BusinessException(ImErrorCode.COLLABORATOR_NOT_EXIST, "协作者不存在");
    }

    /**
     * 抛出文档版本不存在异常
     */
    public static void throwDocumentVersionNotFound() {
        throw new BusinessException(ImErrorCode.DOCUMENT_VERSION_NOT_EXIST, "版本不存在");
    }

    // ========== 用户相关错误抛出方法 ==========

    /**
     * 抛出用户不存在异常
     */
    public static void throwUserNotFound() {
        throw new BusinessException(ImErrorCode.USER_NOT_EXIST, "用户不存在");
    }

    // ========== 消息相关错误抛出方法 ==========

    /**
     * 抛出消息不存在异常
     */
    public static void throwMessageNotFound() {
        throw new BusinessException(ImErrorCode.MESSAGE_NOT_EXIST, "消息不存在");
    }

    // ========== 公告相关错误抛出方法 ==========

    /**
     * 抛出公告不存在异常
     */
    public static void throwAnnouncementNotFound() {
        throw new BusinessException(ImErrorCode.ANNOUNCEMENT_NOT_EXIST, "公告不存在");
    }

    /**
     * 抛出公告不允许评论异常
     */
    public static void throwAnnouncementNoComment() {
        throw new BusinessException(ImErrorCode.ANNOUNCEMENT_NO_COMMENT, "该公告不允许评论");
    }

    // ========== 邮件相关错误抛出方法 ==========

    /**
     * 抛出邮件不存在异常
     */
    public static void throwEmailNotFound() {
        throw new BusinessException(ImErrorCode.EMAIL_NOT_EXIST, "邮件不存在");
    }

    // ========== 应用相关错误抛出方法 ==========

    /**
     * 抛出应用不存在异常
     */
    public static void throwApplicationNotFound() {
        throw new BusinessException(ImErrorCode.APPLICATION_NOT_EXIST, "应用不存在");
    }

    /**
     * 抛出应用编码已存在异常
     */
    public static void throwApplicationCodeExists() {
        throw new BusinessException(ImErrorCode.APPLICATION_CODE_EXISTS, "应用编码已存在");
    }

    // ========== 视频通话相关错误抛出方法 ==========

    /**
     * 抛出对方不在线异常
     */
    public static void throwCallUserOffline() {
        throw new BusinessException(ImErrorCode.CALL_USER_OFFLINE, "对方不在线");
    }

    /**
     * 抛出对方正在通话中异常
     */
    public static void throwCallUserBusy() {
        throw new BusinessException(ImErrorCode.CALL_USER_BUSY, "对方正在通话中");
    }

    /**
     * 抛出无权限接听此通话异常
     */
    public static void throwNoPermissionToAnswer() {
        throw new BusinessException(ImErrorCode.NO_PERMISSION, "无权限接听此通话");
    }

    /**
     * 抛出无权限拒绝此通话异常
     */
    public static void throwNoPermissionToReject() {
        throw new BusinessException(ImErrorCode.NO_PERMISSION, "无权限拒绝此通话");
    }

    /**
     * 抛出无权限结束此通话异常
     */
    public static void throwNoPermissionToEnd() {
        throw new BusinessException(ImErrorCode.NO_PERMISSION, "无权限结束此通话");
    }

    /**
     * 抛出最多支持N人同时通话异常
     */
    public static void throwMaxParticipantsExceeded(int maxParticipants) {
        throw new BusinessException(ImErrorCode.VIDEO_CALL_MAX_PARTICIPANTS_EXCEEDED, "最多支持" + maxParticipants + "人同时通话");
    }

    /**
     * 抛出请邀请至少一人参与通话异常
     */
    public static void throwInviteAtLeastOne() {
        throw new BusinessException(ImErrorCode.VIDEO_CALL_MIN_PARTICIPANTS, "请邀请至少一人参与通话");
    }

    /**
     * 抛出邀请人数超过最大限制异常
     */
    public static void throwInviteExceedsLimit() {
        throw new BusinessException(ImErrorCode.VIDEO_CALL_INVITE_EXCEEDS_LIMIT, "邀请人数超过最大参与者数限制");
    }

    /**
     * 抛出您正在通话中异常
     */
    public static void throwAlreadyInCall() {
        throw new BusinessException(ImErrorCode.VIDEO_CALL_ALREADY_IN_CALL, "您正在通话中，请先结束当前通话");
    }

    /**
     * 抛出该通话不是群组通话异常
     */
    public static void throwNotGroupCall() {
        throw new BusinessException(ImErrorCode.VIDEO_CALL_NOT_GROUP_CALL, "该通话不是群组通话");
    }

    /**
     * 抛出您已在通话中异常
     */
    public static void throwAlreadyJoinedCall() {
        throw new BusinessException(ImErrorCode.VIDEO_CALL_ALREADY_JOINED, "您已在通话中");
    }

    /**
     * 抛出您已离开该通话异常
     */
    public static void throwAlreadyLeftCall() {
        throw new BusinessException(ImErrorCode.VIDEO_CALL_ALREADY_LEFT, "您已离开该通话");
    }

    /**
     * 抛出通话人数已满异常
     */
    public static void throwCallFull() {
        throw new BusinessException(ImErrorCode.VIDEO_CALL_FULL, "通话人数已满");
    }

    /**
     * 抛出您不在此通话中异常
     */
    public static void throwNotInCall() {
        throw new BusinessException(ImErrorCode.VIDEO_CALL_NOT_IN_CALL, "您不在此通话中");
    }

    // ========== 会议相关错误抛出方法 ==========

    /**
     * 抛出只有主持人可以修改会议异常
     */
    public static void throwOnlyHostCanModify() {
        throw new BusinessException(ImErrorCode.NO_PERMISSION, "只有主持人可以修改会议");
    }

    /**
     * 抛出会议已开始或已结束无法修改异常
     */
    public static void throwMeetingCannotModify() {
        throw new BusinessException(ImErrorCode.VIDEO_MEETING_STATUS_ERROR, "会议已开始或已结束，无法修改");
    }

    /**
     * 抛出只有主持人可以取消会议异常
     */
    public static void throwOnlyHostCanCancel() {
        throw new BusinessException(ImErrorCode.NO_PERMISSION, "只有主持人可以取消会议");
    }

    /**
     * 抛出只有预定状态会议可以取消异常
     */
    public static void throwOnlyScheduledCanCancel() {
        throw new BusinessException(ImErrorCode.VIDEO_MEETING_STATUS_ERROR, "只有预定状态的会议可以取消");
    }

    /**
     * 抛出只有主持人可以删除会议异常
     */
    public static void throwOnlyHostCanDelete() {
        throw new BusinessException(ImErrorCode.NO_PERMISSION, "只有主持人可以删除会议");
    }

    /**
     * 抛出只有已结束或已取消会议可以删除异常
     */
    public static void throwOnlyEndedOrCancelledCanDelete() {
        throw new BusinessException(ImErrorCode.VIDEO_MEETING_STATUS_ERROR, "只有已结束或已取消的会议可以删除");
    }

    /**
     * 抛出只有主持人可以开始会议异常
     */
    public static void throwOnlyHostCanStart() {
        throw new BusinessException(ImErrorCode.NO_PERMISSION, "只有主持人可以开始会议");
    }

    /**
     * 抛出会议已被取消异常
     */
    public static void throwMeetingCancelled() {
        throw new BusinessException(ImErrorCode.VIDEO_MEETING_STATUS_ERROR, "会议已被取消");
    }

    /**
     * 抛出会议人数已满异常
     */
    public static void throwMeetingFull() {
        throw new BusinessException(ImErrorCode.MEETING_FULL, "会议人数已满");
    }

    /**
     * 抛出只有主持人可以移除参与者异常
     */
    public static void throwOnlyHostCanRemoveParticipant() {
        throw new BusinessException(ImErrorCode.NO_PERMISSION, "只有主持人可以移除参与者");
    }

    /**
     * 抛出用户不在会议中异常
     */
    public static void throwNotInMeeting() {
        throw new BusinessException(ImErrorCode.VIDEO_MEETING_NOT_EXIST, "用户不在会议中");
    }

    /**
     * 抛出不能移除主持人异常
     */
    public static void throwCannotRemoveHost() {
        throw new BusinessException(ImErrorCode.NO_PERMISSION, "不能移除主持人");
    }

    /**
     * 抛出该会议不允许屏幕共享异常
     */
    public static void throwScreenShareNotAllowed() {
        throw new BusinessException(ImErrorCode.NO_PERMISSION, "该会议不允许屏幕共享");
    }

    /**
     * 抛出只有主持人可以转移权限异常
     */
    public static void throwOnlyHostCanTransfer() {
        throw new BusinessException(ImErrorCode.NO_PERMISSION, "只有主持人可以转移权限");
    }

    // ========== 外部联系人分组相关错误抛出方法 ==========

    /**
     * 抛出分组下还有联系人无法删除异常
     */
    public static void throwGroupHasContacts() {
        throw new BusinessException(ImErrorCode.IN_USE, "分组下还有联系人，无法删除");
    }

    /**
     * 抛出分组名称已存在异常
     */
    public static void throwGroupNameExists() {
        throw new BusinessException(ImErrorCode.NAME_EXISTS, "分组名称已存在");
    }

    /**
     * 抛出名称为空异常
     */
    public static void throwNameEmpty() {
        throw new BusinessException(ImErrorCode.PARAM_ERROR, "名称不能为空");
    }

    /**
     * 抛出文件夹名称已存在异常
     */
    public static void throwFolderNameExists() {
        throw new BusinessException(ImErrorCode.NAME_EXISTS, "同名文件夹已存在");
    }

    /**
     * 抛出只有群主可以设置全员禁言异常
     */
    public static void throwOnlyOwnerCanMuteAll() {
        throw new BusinessException(ImErrorCode.OWNER_ONLY, "只有群主可以设置全员禁言");
    }

    /**
     * 抛出只有群主和管理员可以禁言成员异常
     */
    public static void throwOnlyAdminCanMute() {
        throw new BusinessException(ImErrorCode.NO_PERMISSION, "只有群主和管理员可以禁言成员");
    }

    /**
     * 抛出管理员不能禁言群主和其他管理员异常
     */
    public static void throwCannotMuteAdmin() {
        throw new BusinessException(ImErrorCode.NO_PERMISSION, "管理员不能禁言群主和其他管理员");
    }

    /**
     * 抛出只有群主和管理员可以解除禁言异常
     */
    public static void throwOnlyAdminCanUnmute() {
        throw new BusinessException(ImErrorCode.NO_PERMISSION, "只有群主和管理员可以解除禁言");
    }

    /**
     * 抛出不是群组成员异常
     */
    public static void throwNotGroupMember() {
        throw new BusinessException(ImErrorCode.NOT_IN_GROUP, "您不是该群组成员");
    }

    /**
     * 抛出已提交的内容不能修改异常
     */
    public static void throwCannotModifySubmitted() {
        throw new BusinessException(ImErrorCode.STATUS_ERROR, "已提交的日志不能修改");
    }

    /**
     * 抛出已提交的内容不能删除异常
     */
    public static void throwCannotDeleteSubmitted() {
        throw new BusinessException(ImErrorCode.STATUS_ERROR, "已提交的日志不能删除");
    }

    /**
     * 抛出文件为空异常
     */
    public static void throwFileEmpty() {
        throw new BusinessException(ImErrorCode.FILE_NOT_FOUND, "文件不能为空");
    }

    /**
     * 抛出无效的文件路径异常
     */
    public static void throwInvalidFilePath() {
        throw new BusinessException(ImErrorCode.PARAM_ERROR, "无效的文件路径");
    }

    /**
     * 抛出分组名称为空异常
     */
    public static void throwGroupNameEmpty() {
        throw new BusinessException(ImErrorCode.PARAM_ERROR, "分组名称不能为空");
    }

    /**
     * 抛出新分组名称为空异常
     */
    public static void throwNewGroupNameEmpty() {
        throw new BusinessException(ImErrorCode.PARAM_ERROR, "新分组名称不能为空");
    }

    /**
     * 抛出好友ID列表为空异常
     */
    public static void throwFriendIdListEmpty() {
        throw new BusinessException(ImErrorCode.PARAM_ERROR, "好友ID列表不能为空");
    }

    /**
     * 抛出分组不存在异常
     */
    public static void throwFriendGroupNotFound() {
        throw new BusinessException(ImErrorCode.FRIEND_GROUP_NOT_EXIST, "分组不存在");
    }

    /**
     * 抛出只有发布人可以删除公告异常
     */
    public static void throwOnlyPublisherCanDeleteAnnouncement() {
        throw new BusinessException(ImErrorCode.NO_PERMISSION, "只有发布人可以删除公告");
    }

    /**
     * 抛出只有发布人可以撤回公告异常
     */
    public static void throwOnlyPublisherCanRecallAnnouncement() {
        throw new BusinessException(ImErrorCode.NO_PERMISSION, "只有发布人可以撤回公告");
    }

    /**
     * 抛出只有评论作者或发布人可以删除评论异常
     */
    public static void throwOnlyCommenterOrPublisherCanDeleteComment() {
        throw new BusinessException(ImErrorCode.NO_PERMISSION, "只有评论作者或公告发布人可以删除评论");
    }

    /**
     * 抛出审批已处理异常
     */
    public static void throwApprovalAlreadyProcessed() {
        throw new BusinessException(ImErrorCode.STATUS_ERROR, "审批已处理");
    }

    /**
     * 抛出审批已处理无法撤回异常
     */
    public static void throwApprovalCannotRecall() {
        throw new BusinessException(ImErrorCode.STATUS_ERROR, "审批已处理，无法撤回");
    }

    /**
     * 抛出审批模板已停用异常
     */
    public static void throwTemplateDisabled() {
        throw new BusinessException(ImErrorCode.STATUS_ERROR, "审批模板已停用");
    }

    /**
     * 抛出消息内容为空异常
     */
    public static void throwMessageContentEmpty() {
        throw new BusinessException(ImErrorCode.PARAM_ERROR, "消息内容不能为空");
    }

    /**
     * 抛出用户ID为空异常
     */
    public static void throwUserIdEmpty() {
        throw new BusinessException(ImErrorCode.PARAM_ERROR, "用户ID不能为空");
    }

    /**
     * 抛出文本内容为空异常
     */
    public static void throwTextContentEmpty() {
        throw new BusinessException(ImErrorCode.PARAM_ERROR, "文本内容不能为空");
    }

    /**
     * 抛出翻译内容为空异常
     */
    public static void throwTranslationContentEmpty() {
        throw new BusinessException(ImErrorCode.PARAM_ERROR, "翻译内容不能为空");
    }

    /**
     * 抛出目标语言为空异常
     */
    public static void throwTargetLanguageEmpty() {
        throw new BusinessException(ImErrorCode.PARAM_ERROR, "目标语言不能为空");
    }

    /**
     * 抛出文件上传失败异常
     */
    public static void throwFileUploadFailed() {
        throw new BusinessException(ImErrorCode.FILE_UPLOAD_FAILED, "文件上传失败");
    }

    /**
     * 抛出文件路径不存在异常
     */
    public static void throwFilePathNotFound() {
        throw new BusinessException(ImErrorCode.FILE_NOT_FOUND, "文件路径不存在");
    }

    /**
     * 抛出无法读取图片异常
     */
    public static void throwImageReadError() {
        throw new BusinessException(ImErrorCode.FILE_READ_ERROR, "无法读取图片");
    }

    /**
     * 抛出无权限查看此邮件异常
     */
    public static void throwEmailNoPermission() {
        throw new BusinessException(ImErrorCode.NO_PERMISSION, "无权限查看此邮件");
    }

    /**
     * 抛出接收者为空异常
     */
    public static void throwReceiverEmpty() {
        throw new BusinessException(ImErrorCode.PARAM_ERROR, "接收者不能为空");
    }

    /**
     * 抛出无权限删除此邮件异常
     */
    public static void throwEmailNoDeletePermission() {
        throw new BusinessException(ImErrorCode.NO_PERMISSION, "无权限删除此邮件");
    }

    /**
     * 抛出只有群主和管理员可以发布公告异常
     */
    public static void throwOnlyOwnerOrAdminCanPublishAnnouncement() {
        throw new BusinessException(ImErrorCode.NO_PERMISSION, "只有群主和管理员可以发布公告");
    }

    /**
     * 抛出只有群主、管理员或发送者可以编辑公告异常
     */
    public static void throwOnlyOwnerAdminOrSenderCanEditAnnouncement() {
        throw new BusinessException(ImErrorCode.NO_PERMISSION, "只有群主、管理员或发送者可以编辑公告");
    }

    /**
     * 抛出只有群主或发送者可以撤回公告异常
     */
    public static void throwOnlyOwnerOrSenderCanRecallAnnouncement() {
        throw new BusinessException(ImErrorCode.NO_PERMISSION, "只有群主或发送者可以撤回公告");
    }

    /**
     * 抛出只有群主和管理员可以置顶公告异常
     */
    public static void throwOnlyOwnerOrAdminCanPinAnnouncement() {
        throw new BusinessException(ImErrorCode.NO_PERMISSION, "只有群主和管理员可以置顶公告");
    }

    /**
     * 抛出只有群主、管理员或上传者可以编辑文件信息异常
     */
    public static void throwOnlyOwnerAdminOrUploaderCanEditFile() {
        throw new BusinessException(ImErrorCode.NO_PERMISSION, "只有群主、管理员或上传者可以编辑文件信息");
    }

    /**
     * 抛出只有群主、管理员或上传者可以删除文件异常
     */
    public static void throwOnlyOwnerAdminOrUploaderCanDeleteFile() {
        throw new BusinessException(ImErrorCode.NO_PERMISSION, "只有群主、管理员或上传者可以删除文件");
    }

    /**
     * 抛出无权限下载此文件异常
     */
    public static void throwNoPermissionToDownload() {
        throw new BusinessException(ImErrorCode.NO_PERMISSION, "您没有权限下载此文件");
    }

    /**
     * 抛出只能上传自己的文件异常
     */
    public static void throwOnlyUploadOwnFile() {
        throw new BusinessException(ImErrorCode.NO_PERMISSION, "只能上传自己的文件");
    }

    /**
     * 抛出会议室存在未完成的预订无法删除异常
     */
    public static void throwMeetingRoomHasActiveBookings() {
        throw new BusinessException(ImErrorCode.IN_USE, "会议室存在未完成的预订，无法删除");
    }

    /**
     * 抛出会议室当前不可用异常
     */
    public static void throwMeetingRoomUnavailable() {
        throw new BusinessException(ImErrorCode.MEETING_ROOM_NOT_AVAILABLE, "该会议室当前不可用");
    }

    /**
     * 抛出开始时间不能晚于结束时间异常
     */
    public static void throwInvalidTimeRange() {
        throw new BusinessException(ImErrorCode.PARAM_ERROR, "开始时间不能晚于结束时间");
    }

    /**
     * 抛出不能预订过去的时间异常
     */
    public static void throwCannotBookPastTime() {
        throw new BusinessException(ImErrorCode.PARAM_ERROR, "不能预订过去的时间");
    }

    /**
     * 抛出参会人数超过容纳人数异常
     */
    public static void throwExceedCapacity() {
        throw new BusinessException(ImErrorCode.PARAM_ERROR, "参会人数超过会议室容纳人数");
    }

    /**
     * 抛出预订不存在异常
     */
    public static void throwBookingNotFound() {
        throw new BusinessException(ImErrorCode.BOOKING_NOT_EXIST, "预订不存在");
    }

    /**
     * 抛出只有预订人可以取消预订异常
     */
    public static void throwOnlyBookerCanCancel() {
        throw new BusinessException(ImErrorCode.NO_PERMISSION, "只有预订人可以取消预订");
    }

    /**
     * 抛出该预订无法取消异常
     */
    public static void throwBookingCannotCancel() {
        throw new BusinessException(ImErrorCode.STATUS_ERROR, "该预订无法取消");
    }

    /**
     * 抛出该预订不需要确认异常
     */
    public static void throwBookingNoNeedConfirm() {
        throw new BusinessException(ImErrorCode.STATUS_ERROR, "该预订不需要确认");
    }

    /**
     * 抛出只有预订人可以签到异常
     */
    public static void throwOnlyBookerCanCheckIn() {
        throw new BusinessException(ImErrorCode.NO_PERMISSION, "只有预订人可以签到");
    }

    /**
     * 抛出预订状态不正确异常
     */
    public static void throwBookingStatusError() {
        throw new BusinessException(ImErrorCode.STATUS_ERROR, "预订状态不正确");
    }

    /**
     * 抛出已经签过到了异常
     */
    public static void throwAlreadyCheckedIn() {
        throw new BusinessException(ImErrorCode.STATUS_ERROR, "已经签过到了");
    }

    /**
     * 抛出只有预订人可以签退异常
     */
    public static void throwOnlyBookerCanCheckOut() {
        throw new BusinessException(ImErrorCode.NO_PERMISSION, "只有预订人可以签退");
    }

    /**
     * 抛出请先签到异常
     */
    public static void throwPleaseCheckInFirst() {
        throw new BusinessException(ImErrorCode.STATUS_ERROR, "请先签到");
    }

    /**
     * 抛出已经签退过了异常
     */
    public static void throwAlreadyCheckedOut() {
        throw new BusinessException(ImErrorCode.STATUS_ERROR, "已经签退过了");
    }

    /**
     * 抛出只有预订人可以提交反馈异常
     */
    public static void throwOnlyBookerCanSubmitFeedback() {
        throw new BusinessException(ImErrorCode.NO_PERMISSION, "只有预订人可以提交反馈");
    }

    /**
     * 抛出会议结束后才能提交反馈异常
     */
    public static void throwCanSubmitFeedbackAfterMeeting() {
        throw new BusinessException(ImErrorCode.STATUS_ERROR, "会议结束后才能提交反馈");
    }

    /**
     * 抛出只能审批已提交的日志异常
     */
    public static void throwOnlySubmittedCanApprove() {
        throw new BusinessException(ImErrorCode.STATUS_ERROR, "只能审批已提交的日志");
    }

    /**
     * 抛出会议密码错误异常
     */
    public static void throwMeetingPasswordError() {
        throw new BusinessException(ImErrorCode.PARAM_ERROR, "会议密码错误");
    }

    /**
     * 抛出该会议不允许屏幕共享异常
     */
    public static void throwScreenShareDisabled() {
        throw new BusinessException(ImErrorCode.NO_PERMISSION, "该会议不允许屏幕共享");
    }

    // ========== 公告相关错误抛出方法 ==========

    /**
     * 抛出只有发布人可以删除公告异常
     */
    public static void throwOnlyPublisherCanDelete() {
        throw new BusinessException(ImErrorCode.NO_PERMISSION, "只有发布人可以删除公告");
    }

    /**
     * 抛出只有发布人可以撤回公告异常
     */
    public static void throwOnlyPublisherCanWithdraw() {
        throw new BusinessException(ImErrorCode.NO_PERMISSION, "只有发布人可以撤回公告");
    }

    /**
     * 抛出只有评论作者或公告发布人可以删除评论异常
     */
    public static void throwOnlyCommentAuthorOrPublisherCanDelete() {
        throw new BusinessException(ImErrorCode.NO_PERMISSION, "只有评论作者或公告发布人可以删除评论");
    }
}
