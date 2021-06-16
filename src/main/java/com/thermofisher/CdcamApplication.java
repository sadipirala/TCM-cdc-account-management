package com.thermofisher;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
public class CdcamApplication {

	@Value("${env.name}")
	private String envName;

	public static void main(String[] args) {
		SpringApplication.run(CdcamApplication.class, args);
	}

	@Bean
	public Docket newsApi() {
		return new Docket(DocumentationType.SWAGGER_2)
			.useDefaultResponseMessages(false)
			.groupName("CDCAM V1")
			.apiInfo(apiInfo("1.2"))
			.select()
			.apis(RequestHandlerSelectors.basePackage(messageSource().getMessage("swagger.base.package", null,null)))
			.paths(PathSelectors.regex("^((?!v2).)*$"))
			.build();
	}
	
	@Bean
	public Docket newsApiV2() {
		return new Docket(DocumentationType.SWAGGER_2)
			.groupName("CDCAM V2")
			.select()
			.apis(RequestHandlerSelectors.basePackage(messageSource().getMessage("swagger.base.package", null,null)))
			.paths(PathSelectors.regex("/accounts/v2/.*"))
			.build()
			.apiInfo(apiInfo("2.0"));
	}

	private ApiInfo apiInfo(String apiVersion) {
		return new ApiInfoBuilder()
			.title(messageSource().getMessage("swagger.api.title", null,null) + " (" + envName + ")")
			.description(messageSource().getMessage("swagger.api.description", null,null))
			.version(apiVersion)
			.build();
	}

	@Bean
	public ReloadableResourceBundleMessageSource messageSource() {
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasename("classpath:locale/message");
		return messageSource;
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/cdcam/**")
					.allowedOrigins("*")
					.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
					.allowedHeaders("*")
					.allowCredentials(false)
					.maxAge(3600);
			}
		};
	}

}
