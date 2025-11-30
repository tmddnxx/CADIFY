package com.cadify.cadifyWAS.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "Cadify Web Application Server",
                version = "1.0.0"
        )
)
@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI(){
        String authType = "JWT Token";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(authType);
        Components components = new Components().addSecuritySchemes(authType, new SecurityScheme()
                        .name(authType)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                );
        return new OpenAPI()
                .components(components)
                .addSecurityItem(securityRequirement);
    }
}