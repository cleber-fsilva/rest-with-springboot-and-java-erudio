package br.com.erudio.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI customOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("REST API's RESTfull from 0 with Java, Spring Boot, Kubernetes and Docker")
                        .version("v1")
                        .description("REST API's RESTfull from 0 with Java, Spring Boot, Kubernetes and Docker")
                        .termsOfService("Temos de serviço aqui")
                        .license(new License()
                                .name("Apache 2.0")
                                .url("URL da Licença aqui")));
    }
}
