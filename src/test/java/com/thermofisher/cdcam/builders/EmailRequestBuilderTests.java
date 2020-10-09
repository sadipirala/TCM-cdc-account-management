package com.thermofisher.cdcam.builders;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thermofisher.CdcamApplication;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.UsernameRecoveryEmailRequest;
import com.thermofisher.cdcam.model.dto.UsernameRecoveryDTO;
import com.thermofisher.cdcam.utils.AccountUtils;
import com.thermofisher.cdcam.utils.EmailRequestBuilderUtils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CdcamApplication.class)
public class EmailRequestBuilderTests {
  ObjectMapper mapper = new ObjectMapper();

  @Test
  public void buildUsernameRecoveryEmailRequest_ShouldCorrectlyBuildAUsernameRecoveryEmailRequest()
      throws JsonProcessingException {
    // given
    AccountInfo accountInfo = AccountUtils.getSiteAccount();
    UsernameRecoveryDTO usernameRecoveryDTO =  EmailRequestBuilderUtils.buildUsernameRecoveryDTO();
    UsernameRecoveryEmailRequest usernameRecoveryEmailRequest = EmailRequestBuilderUtils.buildUsernameRecoveryEmailRequest(accountInfo.getUsername());

    // when
    UsernameRecoveryEmailRequest request = EmailRequestBuilder.buildUsernameRecoveryEmailRequest(usernameRecoveryDTO, accountInfo);

    // then
    String usernameRecoveryMock = mapper.writeValueAsString(usernameRecoveryEmailRequest);
    String result = mapper.writeValueAsString(request);
    assertEquals(usernameRecoveryMock, result);
  }
}
