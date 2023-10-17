package com.thermofisher.cdcam.model.cdc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonDeserialize(builder = JWTPublicKey.JWTPublicKeyBuilder.class)
public class JWTPublicKey {
    String n;
    String e;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPOJOBuilder(withPrefix = "")
    public static class JWTPublicKeyBuilder {
    }
}