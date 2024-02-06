package com.thermofisher.cdcam.enums.cdc;

import lombok.Getter;

@Getter
public enum DataCenter {
    US("us"),
    CN("cn");

    private String value;

    DataCenter(String value) {
        this.value = value;
    }

    public static DataCenter getEqualsAs(String value) {
        String dataCenter = value.toLowerCase();

        switch (dataCenter) {
            case "us":
                return DataCenter.US;
            case "cn":
                return DataCenter.CN;
            default:
                throw new IllegalArgumentException("Invalid data center provided.");
        }
    }
}