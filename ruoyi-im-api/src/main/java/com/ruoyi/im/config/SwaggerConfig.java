package com.ruoyi.im.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;
import java.util.List;

@Configuration
@EnableSwagger2
@ConditionalOnProperty(name = "swagger.enabled", havingValue = "true", matchIfMissing = false)
public class SwaggerConfig implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(SwaggerConfig.class);

    @Value("${server.port:8080}")
    private String port;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.ruoyi.im.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("IM System API")
                .description("Instant messaging and collaboration platform")
                .version("1.0.0")
                .contact(new Contact("RuoYi", "", ""))
                .build();
    }

    @Override
    public void run(ApplicationArguments args) {
        String swaggerUiUrl = "http://localhost:" + port + contextPath + "/swagger-ui.html";
        String swaggerApiDocsUrl = "http://localhost:" + port + contextPath + "/v2/api-docs";

        logger.info("Swagger enabled");
        logger.info("Swagger UI URL: {}", swaggerUiUrl);
        logger.info("API Docs URL: {}", swaggerApiDocsUrl);
    }
}
