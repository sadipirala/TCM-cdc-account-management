package com.thermofisher.cdcam.cdc;

import com.gigya.socialize.GSKeyNotFoundException;
import com.gigya.socialize.GSObject;
import com.gigya.socialize.GSRequest;
import com.gigya.socialize.GSResponse;
import com.thermofisher.cdcam.enums.cdc.APIMethods;
import com.thermofisher.cdcam.environment.ApplicationConfiguration;
import com.thermofisher.cdcam.model.AccountInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CDCAccounts {

    final static Logger logger = LogManager.getLogger("CdcamApp");
    ApplicationConfiguration conf;

    public AccountInfo getAccount(String UID) {
        try {
            String apiMethod = APIMethods.GET.getValue();
            GSRequest request = new GSRequest(conf.getCDCApiKey(), conf.getCDCSecretKey(), apiMethod, null, true, conf.getCDCUserKey());
            request.setParam("UID", UID);
            request.setParam("include","emails, profile, data, password,userInfo,regSource,identities");
            request.setParam("extraProfileFields","username, locale,samlData");

            GSResponse response = request.send();
            if (response.getErrorCode() == 0) {
                GSObject obj = response.getData();
                GSObject userInfo = (GSObject) obj.get("userInfo");
                logger.info("User Found: " + obj.get("UID"));
                return AccountInfo.builder()
                        .username( userInfo.containsKey("username") ? userInfo.getString("username") : userInfo.getString("email"))
                        .emailAddress(userInfo.getString("email"))
                        .firstName(userInfo.containsKey("firstName") ? userInfo.getString("firstName"):"")
                        .lastName(userInfo.containsKey("lastName") ? userInfo.getString("lastName"):"")
                        .country(userInfo.containsKey("country") ? userInfo.getString("country"):"")
                        .localeName(userInfo.containsKey("locale") ? userInfo.getString("locale"):"")
                        .loginProvider(obj.containsKey("loginProvider") ? obj.getString("loginProvider"):"")
                        .password(getAlphaNumericString(10))
                        .regAttepmts(0)
                        .build();
            } else {
                logger.error(response.getErrorDetails());
                return null;
            }
        }catch (GSKeyNotFoundException keyNotFoundException) {
            logger.error(keyNotFoundException.getMessage());
            return null;
        }
    }

    static String getAlphaNumericString(int n) {
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvxyz";
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            int index
                    = (int) (AlphaNumericString.length()
                    * Math.random());
            sb.append(AlphaNumericString
                    .charAt(index));
        }
        return sb.toString();
    }

    public CDCAccounts(ApplicationConfiguration applicationConfiguration){
        this.conf = applicationConfiguration;
    }
}
