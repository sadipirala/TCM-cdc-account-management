package com.thermofisher.cdcam.enums.aws;

import lombok.Getter;

@Getter
public enum CdcamSecrets {
    MAIN_DC("cdc-main-secret-key"),
    SECONDARY_DC("cdc-secondary-secret-key"),
    RECAPTCHAV3("reCaptcha-v3-secret-key"),
    RECAPTCHAV2("reCaptcha-v2-secret-key"),
    QUICKSIGHT_ROLE("aws-quicksight-role");

    private String key;

    CdcamSecrets(String secretKey) {
        this.key = secretKey;
    }
}
