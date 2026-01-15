package com.example.TaskListAPI.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("tasklist-api")
                .pathsToMatch("/api/**")
                .build();
    }

    @Bean
    public OpenAPI taskListOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TaskList API")
                        .description("API documentation for TaskList Application")
                        .version("1.0")
                        .contact(new Contact()
                                .name("API Support")
                                .email("support@tasklist.com")
                        )
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")));
    }
}
