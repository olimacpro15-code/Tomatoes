package com.tomatoes.cosechas.Config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Metadatos de la documentación OpenAPI/Swagger: se ve en /swagger-ui.html */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI documentacionApi() {
        return new OpenAPI()
            .info(new Info()
                .title("Tomatoes - Microservicio de Cosechas")
                .version("1.0.0")
                .description("Lotes, cosechas, producción, asignación de empleados, consumo de insumos y reportes de producción."))
            .components(new Components().addSecuritySchemes("bearerAuth",
                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
