package com.thermofisher.cdcam.model;

import org.springframework.http.HttpStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class EmailSentResponse {
  private int statusCode;

  public boolean isSuccess() {
    HttpStatus status = HttpStatus.valueOf(statusCode);
    return status.is2xxSuccessful();
  }
}
