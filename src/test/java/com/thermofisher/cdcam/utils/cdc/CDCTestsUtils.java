package com.thermofisher.cdcam.utils.cdc;

import com.thermofisher.cdcam.enums.cdc.WebhookEvent;

public class CDCTestsUtils {
    
    public static String getWebhookEventBody(WebhookEvent webhookEvent, int numberOfEvents) {
        String events = "";
        
        for (int i = 0; i < numberOfEvents; i++) {
            String event = String.format("{\"type\": \"%s\", \"data\": { \"uid\": \"\", \"newUid\": \"\" }}", webhookEvent.getValue());

            events += event;
            if (i + 1 != numberOfEvents) {
                events += ", ";
            }
        }

        return String.format("{ \"events\": [%s] }", events);
    }
}
