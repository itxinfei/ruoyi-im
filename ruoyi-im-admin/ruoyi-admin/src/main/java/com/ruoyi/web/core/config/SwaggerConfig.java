package com.ruoyi.web.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.ruoyi.common.config.RuoYiConfig;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * Swagger2接口文档配置类
 *
 * 功能说明：
 * 1. 根据配置文件中的 swagger.enabled 属性动态启用/禁用API文档
 * 2. 扫描所有带有 @ApiOperation 注解的方法，生成API文档
 * 3. 使用OpenAPI 3.0规范（DocumentationType.OAS_30）
 *
 * 配置说明：
 * - 在 application.yml 中设置 swagger.enabled=true 启用文档
 * - 在 application.yml 中设置 swagger.enabled=false 禁用文档
 * - 启用后可通过 /swagger-ui.html 访问文档
 *
 * 注意事项：
 * - 本配置与SpringDocConfig都使用 swagger.enabled 配置项
 * - 建议在开发环境启用，生产环境禁用以节省资源
 * - @ConditionalOnProperty 确保配置为true时才加载此配置类
 *
 * @author ruoyi
 */
@Configuration
@ConditionalOnProperty(name = "swagger.enabled", havingValue = "true", matchIfMissing = false)
public class SwaggerConfig
{
    /** 是否开启swagger */
    @Value("${swagger.enabled}")
    private boolean enabled;
    
    /**
     * 创建API
     */
    @Bean
    public Docket createRestApi()
    {
        return new Docket(DocumentationType.OAS_30)
                // 是否启用Swagger
                .enable(enabled)
                // 用来创建该API的基本信息，展示在文档的页面中（自定义展示的信息）
                .apiInfo(apiInfo())
                // 设置哪些接口暴露给Swagger展示
                .select()
                // 扫描所有有注解的api，用这种方式更灵活
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                // 扫描指定包中的swagger注解
                //.apis(RequestHandlerSelectors.basePackage("com.ruoyi.project.tool.swagger"))
                // 扫描所有 .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build();
    }

    /**
     * 添加摘要信息
     */
    private ApiInfo apiInfo()
    {
        // 用ApiInfoBuilder进行定制
        return new ApiInfoBuilder()
                // 设置标题
                .title("标题：若依管理系统_接口文档")
                // 描述
                .description("描述：用于管理集团旗下公司的人员信息,具体包括XXX,XXX模块...")
                // 作者信息
                .contact(new Contact(RuoYiConfig.getName(), null, null))
                // 版本
                .version("版本号:" + RuoYiConfig.getVersion())
                .build();
    }
}
