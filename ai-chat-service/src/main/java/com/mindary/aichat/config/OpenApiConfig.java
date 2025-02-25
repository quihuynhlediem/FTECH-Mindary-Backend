package com.mindary.aichat.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@OpenAPIDefinition
public class OpenApiConfig {
    @Bean
    public OpenAPI openApi(
            @Value("${spring.application.name:application.properties}") String serviceTitle,
            @Value("${spring.application.version:application.properties}") String serviceVersion,
            @Value("${server.port:application.properties}") String url
    ) {
        return new OpenAPI()
                .servers(List.of(new Server().url("http://localhost:8080")))
                .info(new Info().title(serviceTitle).version(serviceVersion));
    }
}

