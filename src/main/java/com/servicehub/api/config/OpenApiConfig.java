package com.servicehub.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI servicehubOpenAPI() {
        return new OpenAPI()
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .info(new Info()
                        .title("ServiceHub API")
                        .description("API REST para gerenciamento de prestadores de servico (providers), "
                                + "incluindo cadastro, consulta, atualizacao e remocao. "
                                + "As rotas protegidas exigem um token JWT (use /auth/login e o botao Authorize).")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("William Lunelli")
                                .email("williamlunelli07@gmail.com"))
                        .license(new License()
                                .name("Uso academico")));
    }
}
