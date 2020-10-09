package com.thermofisher.cdcam.utils;

import com.thermofisher.cdcam.model.EmailUserInfo;
import com.thermofisher.cdcam.model.UsernameRecoveryEmailRequest;
import com.thermofisher.cdcam.model.dto.UsernameRecoveryUserInfoDTO;
import com.thermofisher.cdcam.model.dto.UsernameRecoveryDTO;

public class EmailRequestBuilderUtils {
  private static final String firstName = "first";
  private static final String lastName = "last";
  private static final String email = "armadillo-dillo@mail.com";
  private static final String locale = "en_US";

  public static UsernameRecoveryEmailRequest buildUsernameRecoveryEmailRequest(String username) {
    return UsernameRecoveryEmailRequest.builder()
      .userInfo(buildEmailUserInfo(username))
      .locale(locale)
      .build();
  }

  private static EmailUserInfo buildEmailUserInfo(String username) {
    return EmailUserInfo.builder()
      .firstName(firstName)
      .lastName(lastName)
      .email(email)
      .username(username)
      .redirectUrl("")
      .build();
  }

  public static UsernameRecoveryDTO buildUsernameRecoveryDTO() {
    return UsernameRecoveryDTO.builder()
      .userInfo(buildUserInfoDTO())
      .locale(locale)
      .build();
  }

  private static UsernameRecoveryUserInfoDTO buildUserInfoDTO() {
    return UsernameRecoveryUserInfoDTO.builder()
      .email(email)
      .redirectUrl("")
      .build();
  }
}
