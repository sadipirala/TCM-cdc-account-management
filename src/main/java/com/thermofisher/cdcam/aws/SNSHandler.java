package com.thermofisher.cdcam.aws;

import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.PrintWriter;
import java.io.StringWriter;

@Configuration
public class SNSHandler {
    final static Logger logger = LogManager.getLogger("AWSSecretManager");
    @Value("${aws.sns.client.region}")
    private String region;
    @Value("${aws.sns.topic}")
    private String snsTopic;
    
    public boolean sendSNSNotification(String message) {
        try {
            AmazonSNS snsClient = AmazonSNSClient.builder()
                    .withRegion(region)
                    .withCredentials(new InstanceProfileCredentialsProvider(false))
                    .build();

            String resultPublish = snsClient.publish(snsTopic, message).getMessageId();

            return resultPublish.length() > 0;
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String stackTrace = sw.toString();
            logger.fatal(stackTrace);
            return false;
        }
    }
}
