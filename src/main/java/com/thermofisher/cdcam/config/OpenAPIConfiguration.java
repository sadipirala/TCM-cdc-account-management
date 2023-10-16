package com.thermofisher.cdcam.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class OpenAPIConfiguration {
   // @Bean
    /*public OpenAPI openAPI() {
        Contact contact = new Contact();
        contact.setName("B2B Punch Out  - Digital Engineering");
        contact.setEmail("de-tf-b2b-punchout@thermofisher.com");
        return new OpenAPI()
                .pathsToMatch("/v2/.*")
                .info(new Info().title("B2B POSR App APIs")
                        .description("B2B POSR Spring boot app to support B2B POSR")
                        .version("1.0").contact(contact));
    }*/
     /*
    public Docket newsApi() {
		return new Docket(DocumentationType.SWAGGER_2)
			.useDefaultResponseMessages(false)
			.groupName("CDCAM V1")
			.apiInfo(apiInfo("1.2"))
			.select()
			.apis(RequestHandlerSelectors.basePackage(messageSource().getMessage("swagger.base.package", null,null)))
			.paths(PathSelectors.regex("^((?!v2).)*$"))
			.build();
	}*/
    @Bean
    public GroupedOpenApi Api1() {
        return GroupedOpenApi.builder()
                .group("CDCAM V1")
                .pathsToMatch("^((?!v2).)*$")
                .build() ;
    }

    @Bean
    public GroupedOpenApi Api2() {
        return GroupedOpenApi.builder()
                .group("CDCAM V2")
                .pathsToMatch("/v2/.*")
                .build();
    }
   /*

	@Bean
	public Docket newsApiV2() {
		return new Docket(DocumentationType.SWAGGER_2)
			.groupName("CDCAM V2")
			.select()
			.apis(RequestHandlerSelectors.basePackage(messageSource().getMessage("swagger.base.package", null,null)))
			.paths(PathSelectors.regex("/v2/.*"))
			.build()
			.apiInfo(apiInfo("2.0"));
	}
     */

}
