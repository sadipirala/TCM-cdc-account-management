package com.thermofisher.cdcam.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiConfig {

    @Value("${env.name}")
    private String env;


    @Bean
    public OpenAPI openAPI() {
       return new OpenAPI()
                .info(new Info().title("Customer Data Cloud Account Management("+env+")")
                .version("1.0"));
    }

}
