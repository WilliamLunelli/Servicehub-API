package com.servicehub.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI servicehubOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ServiceHub API")
                        .description("API REST para gerenciamento de prestadores de servico (providers), "
                                + "incluindo cadastro, consulta, atualizacao e remocao.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("William Lunelli")
                                .email("williamlunelli07@gmail.com"))
                        .license(new License()
                                .name("Uso academico")));
    }
}
