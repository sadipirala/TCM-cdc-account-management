package com.thermofisher.cdcam.aws;

import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.thermofisher.cdcam.environment.ApplicationConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SNSHandler {
    @Autowired
    ApplicationConfiguration applicationConfiguration;

    public boolean sendSNSNotification(String snsTopic, String message) {
        AmazonSNS snsClient = AmazonSNSClient.builder()
                .withRegion(applicationConfiguration.getDistStoreAWSClientRegion())
                .withCredentials(new InstanceProfileCredentialsProvider(false))
                .build();

        String resultPublish = snsClient.publish(snsTopic, message).getMessageId();

        return (resultPublish.length() > 0) ? true : false;
    }
}
