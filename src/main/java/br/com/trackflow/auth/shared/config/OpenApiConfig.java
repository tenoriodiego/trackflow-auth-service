
package br.com.trackflow.auth.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI trackFlowOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TrackFlow Auth Service API")
                        .version("1.0.0")
                        .description("""
                                API de autenticação e gerenciamento de usuários
                                do TrackFlow.
                                Permite cadastrar usuários, realizar login,
                                renovar tokens e consultar o usuário autenticado.
                                """)
                        .contact(new Contact()
                                .name("TrackFlow")))
                .components(new Components()
                        .addSecuritySchemes(
                                SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(SECURITY_SCHEME_NAME));
    }
}
