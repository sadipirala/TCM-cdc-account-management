package com.thermofisher.cdcam.enums;

import lombok.Getter;

@Getter
public enum ResponseCode {
    SUCCESS(200),
    LOGINID_ALREADY_EXISTS(4001),
    ACCOUNT_PENDING_VERIFICATION(2061);

    ResponseCode(int value) {
        this.value = value;
    }

    private int value;
}
