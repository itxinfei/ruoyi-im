package com.ruoyi.web.core.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import com.ruoyi.common.config.RuoYiConfig;

/**
 * OpenAPI配置类
 *
 * <p>用于配置Swagger/OpenAPI接口文档的元数据信息</p>
 * <p>提供API文档的标题、描述、版本、联系方式等基础信息</p>
 *
 * @author ruoyi
 * @since 2025-01-07
 */
@Configuration
public class OpenApiConfig {

    /**
     * 配置OpenAPI文档信息Bean
     *
     * <p>创建并返回OpenAPI对象，包含API文档的基本信息</p>
     * <p>包括：标题、描述、版本号、联系信息等</p>
     *
     * @return OpenAPI配置对象
     */
    @Bean
    public OpenAPI api() {
        return new OpenAPI()
                .info(new Info()
                        .title("标题：若依管理系统_接口文档")
                        .description("描述：用于管理集团旗下公司的人员信息,具体包括XXX,XXX模块...")
                        .version("版本号:" + RuoYiConfig.getVersion())
                        .contact(new Contact()
                                .name(RuoYiConfig.getName())
                                .email("")));
    }
}
