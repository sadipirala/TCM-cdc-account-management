package com.thermofisher.cdcam.config;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfiguration {

    @Bean
    public GroupedOpenApi Api1() {
        return GroupedOpenApi.builder()
                .group("CDCAM V1")
                .pathsToMatch("^((?!v3).)*$")
                .build();
    }

    @Bean
    public GroupedOpenApi Api2() {
        return GroupedOpenApi.builder()
                .group("CDCAM V2")
                .pathsToMatch("/v3/.*")
                .build();
    }

}
