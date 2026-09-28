package com.ejemplo.fragmentos.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openApiInfo() {
        return new OpenAPI().info(new Info()
                .title("Fragmentos PDF API")
                .description("Consultas exactas, semánticas e híbridas sobre fragmentos_pdf (PostgreSQL + pgvector + Ollama)")
                .version("1.0.0"));
    }
}
