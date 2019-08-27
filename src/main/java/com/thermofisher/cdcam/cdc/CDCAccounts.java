package com.thermofisher.cdcam.cdc;

import com.gigya.socialize.GSKeyNotFoundException;
import com.gigya.socialize.GSObject;
import com.gigya.socialize.GSRequest;
import com.gigya.socialize.GSResponse;
import com.thermofisher.cdcam.builders.AccountBuilder;
import com.thermofisher.cdcam.enums.cdc.APIMethods;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CDCAccounts {

    final static Logger logger = LogManager.getLogger("CdcamApp");

    @Value("${cdc.apiKey}")
    private String apiKey;

    @Value("${cdc.secretKey}")
    private String secretKey;

    @Value("${cdc.userKey}")
    private String userKey;

    AccountBuilder accountBuilder = new AccountBuilder();
    public AccountInfo getAccount(String UID) {
        try {
            String apiMethod = APIMethods.GET.getValue();
            GSRequest request = new GSRequest(apiKey, secretKey , apiMethod, null, true, userKey);
            request.setParam("UID", UID);
            request.setParam("include","emails, profile, data, password,userInfo,regSource,identities");
            request.setParam("extraProfileFields","username, locale,work");

            GSResponse response = request.send();
            if (response.getErrorCode() == 0) {
                GSObject obj = response.getData();
                logger.info("User Found: " + obj.get("UID"));
                return accountBuilder.getAccountInfo(obj);
            } else {
                logger.error(response.getErrorDetails());
                return null;
            }
        }catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }
}
