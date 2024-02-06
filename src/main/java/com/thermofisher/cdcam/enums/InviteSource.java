package com.thermofisher.cdcam.enums;

public enum InviteSource {
    IAC("IAC"),
    PU("PU");

    private String value;

    InviteSource(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static boolean contains(String source) {
        for (InviteSource inviteSource : InviteSource.values()) {
            if (inviteSource.getValue().equals(source)) return true;
        }

        return false;
    }
}
