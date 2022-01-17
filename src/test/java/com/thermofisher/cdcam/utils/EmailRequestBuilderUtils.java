package com.thermofisher.cdcam.utils;

import com.thermofisher.cdcam.model.dto.UsernameRecoveryDTO;
import com.thermofisher.cdcam.model.dto.UsernameRecoveryUserInfoDTO;

public class EmailRequestBuilderUtils {
  private static final String email = "armadillo-dillo@mail.com";
  private static final String locale = "en_US";

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
