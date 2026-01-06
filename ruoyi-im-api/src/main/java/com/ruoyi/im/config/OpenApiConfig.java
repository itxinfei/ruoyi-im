package com.ruoyi.im.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String port;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Bean
    public OpenAPI api() {
        return new OpenAPI()
                .info(new Info()
                        .title("IM即时通讯系统API文档")
                        .description("提供用户管理、消息发送、会话管理、联系人管理、群组管理等功能的API接口")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("RuoYi")
                                .email("")));
    }
}