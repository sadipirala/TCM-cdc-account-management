package com.thermofisher.cdcam.builders;

import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.EmailUserInfo;
import com.thermofisher.cdcam.model.UsernameRecoveryEmailRequest;
import com.thermofisher.cdcam.model.dto.UsernameRecoveryDTO;
import com.thermofisher.cdcam.utils.EmailLocaleUtils;

public class EmailRequestBuilder {
  
  public static UsernameRecoveryEmailRequest buildUsernameRecoveryEmailRequest(UsernameRecoveryDTO usernameRecoveryDTO, AccountInfo accountInfo) {
    String locale = EmailLocaleUtils.processLocaleForNotification(accountInfo.getLocaleName(), accountInfo.getCountry());
    return UsernameRecoveryEmailRequest.builder()
      .userInfo(buildEmailUserInfo(usernameRecoveryDTO, accountInfo))
      .locale(locale)
      .build();
  }

  private static EmailUserInfo buildEmailUserInfo(UsernameRecoveryDTO usernameRecoveryDTO, AccountInfo accountInfo) {
    return EmailUserInfo.builder()
      .firstName(accountInfo.getFirstName())
      .lastName(accountInfo.getLastName())
      .email(usernameRecoveryDTO.getUserInfo().getEmail())
      .username(accountInfo.getUsername())
      .redirectUrl(usernameRecoveryDTO.getUserInfo().getRedirectUrl())
      .build();
  }
}
