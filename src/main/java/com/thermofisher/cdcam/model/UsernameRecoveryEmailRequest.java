package com.thermofisher.cdcam.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@JsonDeserialize(builder = UsernameRecoveryEmailRequest.UsernameRecoveryEmailRequestBuilder.class)
public class UsernameRecoveryEmailRequest {
  private final String type = "RetrieveUserName";
  private String locale;
  private EmailUserInfo userInfo;
  
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonPOJOBuilder(withPrefix = "")
  public static class UsernameRecoveryEmailRequestBuilder {

  }
}
