package com.thermofisher.cdcam.aws;

import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SNSHandler {

    @Value("${aws.sns.client.region}")
    private String region;
    @Value("${aws.sns.topic}")
    private String snsTopic;
    public boolean sendSNSNotification(String message) {
        AmazonSNS snsClient = AmazonSNSClient.builder()
                .withRegion(region)
                .withCredentials(new InstanceProfileCredentialsProvider(false))
                .build();

        String resultPublish = snsClient.publish(snsTopic, message).getMessageId();

        return (resultPublish.length() > 0) ? true : false;
    }
}
