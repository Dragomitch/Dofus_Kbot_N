package com.dofus.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI / Swagger Configuration
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI dofusOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Dofus Packet Decoder API")
                        .description("REST API for Dofus Retro packet decoding, game state tracking, and navigation")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Dofus Packet Decoder Team")
                                .email("support@dofus-decoder.com"))
                        .license(new License()
                                .name("Educational Use Only")
                                .url("https://github.com/dofus-packet-decoder")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("Production Server")
                ));
    }
}
