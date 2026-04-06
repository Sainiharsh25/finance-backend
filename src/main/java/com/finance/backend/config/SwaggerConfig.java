package com.finance.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Finance Backend API")
                        .description(
                                "Backend for a finance dashboard system.\n\n" +
                                        "## How to use\n" +
                                        "1. Call `POST /api/auth/login` with your credentials\n" +
                                        "2. Copy the `token` from the response\n" +
                                        "3. Click the **Authorize** button above and paste the token\n" +
                                        "4. All endpoints are now unlocked\n\n" +
                                        "## Default users\n" +
                                        "| Email | Password | Role |\n" +
                                        "|---|---|---|\n" +
                                        "| admin@finance.com | admin123 | ADMIN |\n" +
                                        "| analyst@finance.com | analyst123 | ANALYST |\n" +
                                        "| viewer@finance.com | viewer123 | VIEWER |"
                        )
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Harsh Saini")
                                .email("harshsaini00025@gmail.com")
                        )
                )
                .servers(List.of(
                        new Server().url("https://finance-backend-gi1t.onrender.com").description("Live server"),
                        new Server().url("http://localhost:8080").description("Local development")
                ))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Paste your JWT token here. Get it from POST /api/auth/login")
                        )
                );
    }
}



