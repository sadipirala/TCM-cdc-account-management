package com.thermofisher.cdcam.aws;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SNSHandler {

    @Value("${aws.sns.client.region}")
    private String region;


    public void sendNotification(@NotBlank String message, @NotBlank String snsTopic) {
        publishNotification(message, snsTopic, null);
    }

    public void sendNotification(@NotBlank String message, @NotBlank String snsTopic, @NotNull Map<String, MessageAttributeValue> messageAttributes) {
        publishNotification(message, snsTopic, messageAttributes);
    }

    private void publishNotification(@NotBlank String message, @NotBlank String snsTopic, @NotNull Map<String, MessageAttributeValue> messageAttributes) {
        log.info(String.format("Posting SNS message to topic: %s", snsTopic));

        final PublishRequest request = new PublishRequest(snsTopic, message);

        if (messageAttributes != null) {
            request.withMessageAttributes(messageAttributes);
        }

        AmazonSNS snsClient = AmazonSNSClient.builder()
            .withRegion(region)
            .withCredentials(new InstanceProfileCredentialsProvider(false))
            .build();

        snsClient.publish(request);
    }
}
