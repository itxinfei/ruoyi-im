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
