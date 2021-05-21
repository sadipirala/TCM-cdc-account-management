package com.thermofisher.cdcam.models.notifications;

import static org.junit.Assert.assertEquals;

import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.notifications.MergedAccountNotification;
import com.thermofisher.cdcam.utils.AccountUtils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)

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
        assertEquals(result.getMember(), accountInfo.getMember());
    }
}
