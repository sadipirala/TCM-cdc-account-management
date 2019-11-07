// package com.thermofisher.cdcam;

// import static org.mockito.ArgumentMatchers.any;

// import com.fasterxml.jackson.databind.node.JsonNodeFactory;
// import com.fasterxml.jackson.databind.node.ObjectNode;
// import com.thermofisher.CdcamApplication;
// import com.thermofisher.cdcam.cdc.CDCAccounts;
// import com.thermofisher.cdcam.config.SpringAsyncConfig;
// import com.thermofisher.cdcam.services.CDCAccountsService;
// import com.thermofisher.cdcam.services.UpdateAccountService;
// import com.thermofisher.cdcam.utils.AccountInfoUtils;

// import org.junit.Test;
// import org.junit.runner.RunWith;
// import org.mockito.Mock;
// import org.mockito.Mockito;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.test.context.ActiveProfiles;
// import org.springframework.test.context.ContextConfiguration;
// import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

// /**
//  * UpdateAccountServiceTest
//  */
// @ActiveProfiles("test")
// @RunWith(SpringJUnit4ClassRunner.class)
// @SpringBootTest(classes = CdcamApplication.class)
// @ContextConfiguration(classes = { SpringAsyncConfig.class })
// public class UpdateAccountServiceTest {

//     @Autowired
//     UpdateAccountService updateAccountService;

//     @Mock
//     CDCAccountsService cdcAccountService;

//     @Mock
//     CDCAccounts cdcAccounts;

//     @Test
//     public void asyncTest() throws InterruptedException {
//         // given
//         ObjectNode response = JsonNodeFactory.instance.objectNode();
//         response.put("code", "200");
//         response.put("log", "Log");
//         response.put("error", "Error");
//         String uid = "";
//         String email = AccountInfoUtils.emailAddress;
//         Mockito.when(cdcAccountService.update(any())).thenReturn(response);

//         // when
//         updateAccountService.updateLegacyDataInCDC(uid, email);
//         Thread.sleep(10000);
//     }
// }