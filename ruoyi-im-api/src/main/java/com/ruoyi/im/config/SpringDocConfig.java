package com.ruoyi.im.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

/**
 * SpringDoc API文档配置类（OpenAPI 3.0）
 * <p>
 * 功能说明：
 * 1. 根据配置文件中的 swagger.enabled 属性动态启用/禁用本配置类
 * 2. 将IM模块的API接口按功能模块分组，便于管理和查看
 * 3. 启动时在控制台输出API文档访问地址
 * 4. 配置JWT认证方式
 * 5. 添加通用API文档信息
 * <p>
 * 配置说明：
 * - 在 application.yml 中设置 swagger.enabled=true 启用本配置类
 * - 在 application.yml 中设置 swagger.enabled=false 禁用本配置类
 * - 启用后可通过 http://localhost:8080/swagger-ui.html 访问文档
 * <p>
 * 分组说明：
 * - IM Chat: 消息、历史记录相关接口
 * - IM Conversation: 会话管理相关接口
 * - IM Contact: 联系人相关接口
 * - IM Group: 群组相关接口
 * - IM User: 用户管理相关接口
 * - IM File: 文件管理相关接口
 * - IM Video Call: 视频通话相关接口
 * - IM Email: 邮件相关接口
 * - IM Document: 文档相关接口
 * - IM Ding: 钉钉消息相关接口
 * - IM Approval: 审批相关接口
 * - IM Attendance: 考勤相关接口
 * - IM Work Report: 工作报告相关接口
 * - IM Schedule: 日程相关接口
 * - IM Notification: 通知相关接口
 * - IM Workbench: 工作台相关接口
 * - IM Application: 应用中心相关接口
 * - IM Organization: 组织架构相关接口
 * - IM External Contact: 外部联系人相关接口
 * - IM Audit: 审计日志相关接口
 * - IM Monitor: 监控相关接口
 * - IM Health: 健康检查相关接口
 * - IM Auth: 认证相关接口
 * - IM System: 系统配置相关接口
 *
 * @author ruoyi
 */
@Configuration
@ConditionalOnProperty(name = "swagger.enabled", havingValue = "true", matchIfMissing = true)
public class SpringDocConfig implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SpringDocConfig.class);

    @Value("${server.port:8080}")
    private String port;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    // ==================== API分组定义 ====================

    /**
     * 消息模块API
     * 包含：发送消息、获取消息列表、撤回消息、编辑消息、转发消息、搜索消息、标记已读等
     */
    @Bean
    public GroupedOpenApi messageApi() {
        return GroupedOpenApi.builder()
                .group("消息管理")
                .pathsToMatch("/api/im/message/**")
                .build();
    }

    /**
     * 会话模块API
     * 包含：获取会话列表、创建会话、删除会话、置顶会话、免打扰设置、标记已读等
     */
    @Bean
    public GroupedOpenApi conversationApi() {
        return GroupedOpenApi.builder()
                .group("会话管理")
                .pathsToMatch("/api/im/conversation/**", "/api/im/session/**")
                .build();
    }

    /**
     * 联系人模块API
     * 包含：获取联系人列表、添加好友、删除好友、更新备注、好友分组等
     */
    @Bean
    public GroupedOpenApi contactApi() {
        return GroupedOpenApi.builder()
                .group("联系人管理")
                .pathsToMatch("/api/im/contact/**")
                .build();
    }

    /**
     * 群组模块API
     * 包含：创建群组、更新群组、删除群组、添加成员、移除成员、群组公告、群组文件等
     */
    @Bean
    public GroupedOpenApi groupApi() {
        return GroupedOpenApi.builder()
                .group("群组管理")
                .pathsToMatch("/api/im/group/**", "/api/im/groups/**")
                .build();
    }

    /**
     * 用户模块API
     * 包含：用户信息查询、用户列表、用户状态更新、个人资料修改等
     */
    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("用户管理")
                .pathsToMatch("/api/im/user/**")
                .build();
    }

    /**
     * 文件模块API
     * 包含：文件上传、文件下载、文件列表、文件删除、文件预览、文件分享等
     */
    @Bean
    public GroupedOpenApi fileApi() {
        return GroupedOpenApi.builder()
                .group("文件管理")
                .pathsToMatch("/api/im/file/**")
                .build();
    }

    /**
     * 视频通话模块API
     * 包含：发起通话、接听通话、挂断通话、拒绝通话、信令处理等
     */
    @Bean
    public GroupedOpenApi videoCallApi() {
        return GroupedOpenApi.builder()
                .group("视频通话")
                .pathsToMatch("/api/im/video-call/**")
                .build();
    }

    /**
     * 邮件模块API
     * 包含：发送邮件、收件箱、发件箱、草稿箱、邮件删除等
     */
    @Bean
    public GroupedOpenApi emailApi() {
        return GroupedOpenApi.builder()
                .group("邮件管理")
                .pathsToMatch("/api/im/email/**")
                .build();
    }

    /**
     * 文档模块API
     * 包含：文档创建、文档编辑、文档分享、文档删除、协作编辑等
     */
    @Bean
    public GroupedOpenApi documentApi() {
        return GroupedOpenApi.builder()
                .group("文档协作")
                .pathsToMatch("/api/im/document/**")
                .build();
    }

    /**
     * 钉钉消息模块API
     * 包含：发送钉钉消息、已读回执、消息撤回等
     */
    @Bean
    public GroupedOpenApi dingApi() {
        return GroupedOpenApi.builder()
                .group("钉钉消息")
                .pathsToMatch("/api/im/ding/**")
                .build();
    }

    /**
     * 审批模块API
     * 包含：发起审批、审批操作、审批历史、审批抄送等
     */
    @Bean
    public GroupedOpenApi approvalApi() {
        return GroupedOpenApi.builder()
                .group("审批管理")
                .pathsToMatch("/api/im/approval/**")
                .build();
    }

    /**
     * 考勤模块API
     * 包含：打卡签到、签退、考勤记录、考勤统计等
     */
    @Bean
    public GroupedOpenApi attendanceApi() {
        return GroupedOpenApi.builder()
                .group("考勤管理")
                .pathsToMatch("/api/im/attendance/**")
                .build();
    }

    /**
     * 工作报告模块API
     * 包含：创建报告、查看报告、报告评论、报告点赞等
     */
    @Bean
    public GroupedOpenApi workReportApi() {
        return GroupedOpenApi.builder()
                .group("工作报告")
                .pathsToMatch("/api/im/work-report/**")
                .build();
    }

    /**
     * 日程模块API
     * 包含：创建日程、日程列表、日程更新、日程删除、日程分享等
     */
    @Bean
    public GroupedOpenApi scheduleApi() {
        return GroupedOpenApi.builder()
                .group("日程管理")
                .pathsToMatch("/api/im/schedule/**")
                .build();
    }

    /**
     * 通知模块API
     * 包含：通知列表、通知标记已读、通知删除等
     */
    @Bean
    public GroupedOpenApi notificationApi() {
        return GroupedOpenApi.builder()
                .group("通知管理")
                .pathsToMatch("/api/im/notification/**")
                .build();
    }

    /**
     * 工作台模块API
     * 包含：工作台数据、待办事项、常用应用等
     */
    @Bean
    public GroupedOpenApi workbenchApi() {
        return GroupedOpenApi.builder()
                .group("工作台")
                .pathsToMatch("/api/im/workbench/**")
                .build();
    }

    /**
     * 应用中心模块API
     * 包含：应用列表、应用安装、应用卸载、应用配置等
     */
    @Bean
    public GroupedOpenApi applicationApi() {
        return GroupedOpenApi.builder()
                .group("应用中心")
                .pathsToMatch("/api/im/app/**")
                .build();
    }

    /**
     * 组织架构模块API
     * 包含：部门列表、成员列表、组织架构树等
     */
    @Bean
    public GroupedOpenApi organizationApi() {
        return GroupedOpenApi.builder()
                .group("组织架构")
                .pathsToMatch("/api/im/org/**")
                .build();
    }

    /**
     * 外部联系人模块API
     * 包含：外部联系人列表、添加外部联系人、外部联系人分组等
     */
    @Bean
    public GroupedOpenApi externalContactApi() {
        return GroupedOpenApi.builder()
                .group("外部联系人")
                .pathsToMatch("/api/im/external-contact/**")
                .build();
    }

    /**
     * 审计日志模块API
     * 包含：审计日志查询、日志导出、日志统计等
     */
    @Bean
    public GroupedOpenApi auditApi() {
        return GroupedOpenApi.builder()
                .group("审计日志")
                .pathsToMatch("/api/im/audit/**")
                .build();
    }

    /**
     * 监控模块API
     * 包含：系统监控、性能监控、在线用户统计等
     */
    @Bean
    public GroupedOpenApi monitorApi() {
        return GroupedOpenApi.builder()
                .group("系统监控")
                .pathsToMatch("/api/im/monitor/**")
                .build();
    }

    /**
     * 健康检查模块API
     * 包含：健康检查、组件状态、依赖检查等
     */
    @Bean
    public GroupedOpenApi healthApi() {
        return GroupedOpenApi.builder()
                .group("健康检查")
                .pathsToMatch("/api/im/health/**")
                .build();
    }

    /**
     * 认证模块API
     * 包含：用户登录、用户登出、token刷新等
     */
    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("认证授权")
                .pathsToMatch("/api/im/auth/**")
                .build();
    }

    /**
     * 系统配置模块API
     * 包含：系统配置、敏感词管理、备份恢复等
     */
    @Bean
    public GroupedOpenApi systemApi() {
        return GroupedOpenApi.builder()
                .group("系统配置")
                .pathsToMatch("/api/im/config/**", "/api/im/backup/**", "/api/im/sensitive-word/**")
                .build();
    }

    /**
     * 消息已读模块API
     * 包含：标记已读、已读回执、已读统计等
     */
    @Bean
    public GroupedOpenApi messageReadApi() {
        return GroupedOpenApi.builder()
                .group("消息已读")
                .pathsToMatch("/api/im/message-read/**")
                .build();
    }

    /**
     * @提及模块API
     * 包含：@用户、@所有人、提及列表等
     */
    @Bean
    public GroupedOpenApi mentionApi() {
        return GroupedOpenApi.builder()
                .group("@提及")
                .pathsToMatch("/api/im/mention/**")
                .build();
    }

    /**
     * 消息收藏模块API
     * 包含：收藏消息、取消收藏、收藏列表等
     */
    @Bean
    public GroupedOpenApi messageFavoriteApi() {
        return GroupedOpenApi.builder()
                .group("消息收藏")
                .pathsToMatch("/api/im/favorite/**")
                .build();
    }

    /**
     * 群组文件模块API
     * 包含：群组文件列表、文件上传、文件删除等
     */
    @Bean
    public GroupedOpenApi groupFileApi() {
        return GroupedOpenApi.builder()
                .group("群组文件")
                .pathsToMatch("/api/im/group-file/**")
                .build();
    }

    /**
     * 群组公告模块API
     * 包含：公告列表、发布公告、删除公告等
     */
    @Bean
    public GroupedOpenApi groupAnnouncementApi() {
        return GroupedOpenApi.builder()
                .group("群组公告")
                .pathsToMatch("/api/im/group-announcement/**")
                .build();
    }

    /**
     * 群组禁言模块API
     * 包含：成员禁言、取消禁言、全员禁言等
     */
    @Bean
    public GroupedOpenApi groupMuteApi() {
        return GroupedOpenApi.builder()
                .group("群组禁言")
                .pathsToMatch("/api/im/group-mute/**")
                .build();
    }

    /**
     * 文件分片上传模块API
     * 包含：初始化上传、上传分片、合并分片、取消上传等
     */
    @Bean
    public GroupedOpenApi fileChunkUploadApi() {
        return GroupedOpenApi.builder()
                .group("文件分片上传")
                .pathsToMatch("/api/im/file/chunk/**")
                .build();
    }

    /**
     * 文件预览模块API
     * 包含：获取预览URL、生成缩略图、预览支持检查等
     */
    @Bean
    public GroupedOpenApi filePreviewApi() {
        return GroupedOpenApi.builder()
                .group("文件预览")
                .pathsToMatch("/api/im/file/preview/**")
                .build();
    }

    /**
     * 任务管理模块API
     * 包含：任务创建、分配、跟踪、统计等
     */
    @Bean
    public GroupedOpenApi taskApi() {
        return GroupedOpenApi.builder()
                .group("任务管理")
                .pathsToMatch("/api/im/task/**")
                .build();
    }

    /**
     * 公告管理模块API
     * 包含：公告发布、管理、已读统计等
     */
    @Bean
    public GroupedOpenApi announcementApi() {
        return GroupedOpenApi.builder()
                .group("公告管理")
                .pathsToMatch("/api/im/announcement/**")
                .build();
    }

    /**
     * 会议室管理模块API
     * 包含：会议室管理、预订、签到等
     */
    @Bean
    public GroupedOpenApi meetingRoomApi() {
        return GroupedOpenApi.builder()
                .group("会议室管理")
                .pathsToMatch("/api/im/meeting-room/**")
                .build();
    }

    /**
     * 首页模块API
     * 包含：首页数据、快捷入口等
     */
    @Bean
    public GroupedOpenApi homeApi() {
        return GroupedOpenApi.builder()
                .group("首页")
                .pathsToMatch("/", "/api/im/home/**")
                .build();
    }

    // ==================== OpenAPI文档信息 ====================

    /**
     * 配置OpenAPI文档信息
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // API基本信息
                .info(new Info()
                        .title("RuoYi-IM API文档")
                        .description("企业级即时通讯系统接口文档")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("RuoYi-IM 开发团队")
                                .email("support@ruoyi.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                // 安全配置（JWT认证）
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("请输入JWT Token，格式：Bearer {token}")))
                // 全局安全要求（所有接口默认需要JWT认证）
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth", Collections.emptyList()));
    }

    // ==================== 启动输出 ====================

    @Override
    public void run(ApplicationArguments args) {
        String swaggerUiUrl = "http://localhost:" + port + contextPath + "/swagger-ui.html";
        String apiDocsUrl = "http://localhost:" + port + contextPath + "/v3/api-docs";

        log.info("=============================================");
        log.info("RuoYi-IM API文档已启用");
        log.info("Swagger UI地址: {}", swaggerUiUrl);
        log.info("OpenAPI JSON地址: {}", apiDocsUrl);
        log.info("=============================================");
    }
}
