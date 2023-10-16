package com.thermofisher.cdcam.models.notifications;

import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.notifications.MergedAccountNotification;
import com.thermofisher.cdcam.utils.AccountUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class MergedAccountNotificationTests {
    
    @Test
    public void buildFrom_ShouldBuildAMergedAccountNotificationFromAccountInfo() {
        // given
        AccountInfo accountInfo = AccountUtils.getSiteAccount();

        // when
        MergedAccountNotification result = MergedAccountNotification.build(accountInfo);

        // then
        assertEquals(result.getUid(), accountInfo.getUid());
        assertEquals(result.getPassword(), accountInfo.getPassword());
        assertEquals(result.getCompany(), accountInfo.getCompany());
        assertEquals(result.getCity(), accountInfo.getCity());
        assertEquals(result.getCountry(), accountInfo.getCountry());
        assertEquals(result.isMarketingConsent(), accountInfo.isMarketingConsent());
    }
}
