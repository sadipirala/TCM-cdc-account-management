package com.thermofisher.cdcam.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import com.thermofisher.cdcam.utils.Utils;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class CIPAuthDataDTO {

    @SerializedName("client_id")
    @JsonProperty("client_id")
    private String clientId;

    @SerializedName("redirect_uri")
    @JsonProperty("redirect_uri")
    private String redirectUri;

    @SerializedName("state")
    @JsonProperty("state")
    private String state;

    @SerializedName("scope")
    @JsonProperty("scope")
    private String scope;

    @SerializedName("response_type")
    @JsonProperty("response_type")
    private String responseType;

    @JsonIgnore
    public boolean areClientIdAndRedirectUriInvalid() {
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
