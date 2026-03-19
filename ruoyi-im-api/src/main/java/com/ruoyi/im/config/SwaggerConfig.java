package com.ruoyi.im.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI 配置
 * 
 * @author ruoyi
 */
@Configuration
public class SwaggerConfig {

    @Value("${swagger.title:若依IM即时通讯系统API}")
    private String title;

    @Value("${swagger.description:若依IM即时通讯系统API文档}")
    private String description;

    @Value("${swagger.version:1.0.0}")
    private String version;

    @Value("${swagger.author:ruoyi}")
    private String author;

    @Value("${swagger.license:Apache 2.0}")
    private String license;

    @Bean
    public OpenAPI customOpenAPI() {
        Contact contact = new Contact();
        contact.setName(author);
        contact.setUrl("https://gitee.com/y_project/RuoYi-IM");

        License licenseObj = new License();
        licenseObj.setName(license);
        licenseObj.setUrl("https://www.apache.org/licenses/LICENSE-2.0");

        Info info = new Info();
        info.setTitle(title);
        info.setDescription(description);
        info.setVersion(version);
        info.setContact(contact);
        info.setLicense(licenseObj);

        OpenAPI openAPI = new OpenAPI();
        openAPI.setInfo(info);
        return openAPI;
    }
}
