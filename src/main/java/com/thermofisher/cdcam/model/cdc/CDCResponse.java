package com.thermofisher.cdcam.model.cdc;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class CDCResponse {
    private int statusCode;
    private int errorCode;
    private String statusReason;
    private String callId;
    private String time;
}
