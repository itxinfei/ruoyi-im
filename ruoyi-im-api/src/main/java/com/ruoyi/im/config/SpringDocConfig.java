package com.ruoyi.im.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.boot.ApplicationArguments;

/**
 * SpringDoc API文档配置类（OpenAPI 3.0）
 *
 * 功能说明：
 * 1. 根据配置文件中的 swagger.enabled 属性动态启用/禁用本配置类
 * 2. 将IM模块的API接口按功能模块分组，便于管理和查看
 * 3. 启动时在控制台输出API文档访问地址
 *
 * 配置说明：
 * - 在 application.yml 中设置 swagger.enabled=true 启用本配置类
 * - 在 application.yml 中设置 swagger.enabled=false 禁用本配置类
 * - 启用后可通过 http://localhost:8080/swagger-ui.html 访问文档
 *
 * 重要说明：
 * - swagger.enabled 配置项只控制本配置类，不影响 SpringDoc 框架本身
 * - SpringDoc 框架的配置项是 springdoc.api-docs.enabled 和 springdoc.swagger-ui.enabled
 * - 如果要完全禁用 SpringDoc，需要同时设置 springdoc.api-docs.enabled=false 和 springdoc.swagger-ui.enabled=false
 *
 * 分组说明：
 * - IM Chat: 消息、历史记录、会话相关接口
 * - Contact: 联系人相关接口
 * - Workbench: 工作台相关接口
 * - Approval: 审批相关接口
 * - Application Center: 应用中心相关接口
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

    @Bean
    public GroupedOpenApi imChatApi() {
        return GroupedOpenApi.builder()
                .group("IM Chat")
                .pathsToMatch("/api/im/message/**", "/api/im/history/**", "/api/im/sessions/**")
                .build();
    }

    @Bean
    public GroupedOpenApi contactApi() {
        return GroupedOpenApi.builder()
                .group("Contact")
                .pathsToMatch("/api/im/contact/**")
                .build();
    }

    @Bean
    public GroupedOpenApi workbenchApi() {
        return GroupedOpenApi.builder()
                .group("Workbench")
                .pathsToMatch("/api/im/workbench/**")
                .build();
    }

    @Bean
    public GroupedOpenApi approvalApi() {
        return GroupedOpenApi.builder()
                .group("Approval")
                .pathsToMatch("/api/im/approval/**")
                .build();
    }

    @Bean
    public GroupedOpenApi applicationApi() {
        return GroupedOpenApi.builder()
                .group("Application Center")
                .pathsToMatch("/api/im/app/**")
                .build();
    }

    @Override
    public void run(ApplicationArguments args) {
        String springDocUrl = "http://localhost:" + port + contextPath + "/swagger-ui.html";

        log.info("=============================================");
        log.info("SpringDoc API Doc enabled");
        log.info("SpringDoc UI URL: {}", springDocUrl);
        log.info("=============================================");
    }
}
