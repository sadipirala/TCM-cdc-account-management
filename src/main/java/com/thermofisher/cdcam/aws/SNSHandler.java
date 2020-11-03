package com.thermofisher.cdcam.aws;

import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

@Configuration
public class SNSHandler {

    @Value("${aws.sns.client.region}")
    private String region;

    private final static Logger logger = LogManager.getLogger("AWSSecretManager");
    
    public boolean sendSNSNotification(String message, String snsTopic) {
        return sendSNS(message, snsTopic, null);
    }

    public boolean sendSNSNotification(String message, String snsTopic, Map<String, MessageAttributeValue> messageAttributes) {
        return sendSNS(message, snsTopic, messageAttributes);
    }

    private boolean sendSNS(String message, String snsTopic, Map<String, MessageAttributeValue> messageAttributes) {
        try {
            logger.info(String.format("Posting SNS message to topic: %s", snsTopic));

            final PublishRequest request = new PublishRequest(snsTopic, message);

            if (messageAttributes != null) {
                request.withMessageAttributes(messageAttributes);
            }

            AmazonSNS snsClient = AmazonSNSClient.builder()
                    .withRegion(region)
                    .withCredentials(new InstanceProfileCredentialsProvider(false))
                    .build();

            String resultPublish = snsClient.publish(request).getMessageId();

            return resultPublish.length() > 0;
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String stackTrace = sw.toString();
            logger.error(String.format("An error occurred while sending an SNS message for topic: %s. Error: %s", snsTopic, stackTrace));
            return false;
        }
    }
}
