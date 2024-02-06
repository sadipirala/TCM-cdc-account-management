package com.thermofisher.cdcam.utils.cdc;

import com.thermofisher.cdcam.model.cdc.Thermofisher;

public class ThermofisherAttributesHandler {
    private final String legacyUsername;

    public ThermofisherAttributesHandler(Thermofisher thermofisher) {
        legacyUsername = thermofisher != null ? thermofisher.getLegacyUsername() : null;
    }

    public String getLegacyUsername() {
        return legacyUsername != null ? legacyUsername : null;
    }
}
