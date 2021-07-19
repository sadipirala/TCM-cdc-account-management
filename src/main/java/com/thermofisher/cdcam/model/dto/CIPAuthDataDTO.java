package com.thermofisher.cdcam.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thermofisher.cdcam.utils.Utils;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class CIPAuthDataDTO {

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("redirect_uri")
    private String redirectUri;

    @JsonProperty("state")
    private String state;

    @JsonProperty("scope")
    private String scope;

    @JsonProperty("response_type")
    private String responseType;

    @JsonIgnore
    public boolean areClientIdAndRedirectUriValid() {
        return (Utils.isNullOrEmpty(this.clientId) || Utils.isNullOrEmpty(this.redirectUri));
    }

    @JsonIgnore
    public boolean isCipAuthDataValid() {
        return this.clientId != null &&
                this.redirectUri != null &&
                this.scope != null &&
                this.responseType != null;
    }

}
