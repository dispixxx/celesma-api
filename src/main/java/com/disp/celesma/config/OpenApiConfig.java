package com.disp.celesma.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI celesmaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Celesma API")
                        .description("REST API для управления проектами и задачами Celesma")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth", new SecurityScheme()
                                .name("BearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Вставьте JWT токен (без префикса Bearer). "
                                        + "Получить токен можно через POST /api/v1/auth/login или /api/v1/auth/register")));
    }
}