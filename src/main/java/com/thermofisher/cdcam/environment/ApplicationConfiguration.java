package com.thermofisher.cdcam.environment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.Environment;

@Configuration
@PropertySources({
        @PropertySource(ignoreResourceNotFound=true,value="classpath:application.properties"),
        @PropertySource(ignoreResourceNotFound=true,value="classpath:application-${spring.profiles.active}.properties"),
})
public class ApplicationConfiguration {
    @Autowired
    private Environment env;

    public String getEnvName() { return env.getProperty("env.name"); }

    //CDC
    public String getCDCApiKey(){return env.getProperty("cdc.apiKey");}
    public String getCDCUserKey(){return env.getProperty("cdc.userKey");}
    public String getCDCSecretKey(){return env.getProperty("cdc.secretKey");}

    //aws
    public String getDistStoreAWSClientRegion() { return env.getProperty("aws.sns.client.region"); }
    public String getAWSSNSTopic() { return env.getProperty("aws.sns.topic"); }
}
