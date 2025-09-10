package com.example.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Swagger/OpenAPI 3 설정 및 URL 리다이렉트 설정
 *
 * 접속 가능한 URL:
 * - http://localhost:8080/swagger-ui/index.html (기본)
 * - http://localhost:8080/swagger (리다이렉트)
 * - http://localhost:8080/swagger/ (리다이렉트)
 * - http://localhost:8080/docs (리다이렉트)
 * - http://localhost:8080/api-docs (리다이렉트)
 * - http://localhost:8080/swagger-ui (리다이렉트)
 */
@Configuration
public class SwaggerConfig implements WebMvcConfigurer {

    /**
     * OpenAPI 설정
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("개발 서버"),
                        new Server().url("https://api.settlement.com").description("운영 서버")
                ));
    }

    private Info apiInfo() {
        return new Info()
                .title("Spring Batch Settlement System API")
                .description("정산 시스템 REST API 문서")
                .version("1.0.0")
                .contact(new Contact()
                        .name("Settlement Team")
                        .email("settlement@example.com")
                        .url("https://github.com/settlement-team"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    /**
     * Swagger UI 접속을 위한 URL 리다이렉트 설정
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/swagger", "/swagger-ui/index.html");
        registry.addRedirectViewController("/swagger/", "/swagger-ui/index.html");
        registry.addRedirectViewController("/swagger-ui", "/swagger-ui/index.html");
        registry.addRedirectViewController("/docs", "/swagger-ui/index.html");
        registry.addRedirectViewController("/api-docs", "/swagger-ui/index.html");
    }
}
