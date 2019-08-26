package com.thermofisher.cdcam.builders;
import com.gigya.socialize.GSObject;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.utils.Utils;

public class AccountBuilder {

    public AccountInfo getAccountInfo(GSObject userInfo, GSObject obj){
        try {
            return AccountInfo.builder()
                     .username( userInfo.containsKey("username") ? userInfo.getString("username") : userInfo.getString("email"))
                     .emailAddress(userInfo.getString("email"))
                     .firstName(userInfo.containsKey("firstName") ? userInfo.getString("firstName"):"")
                     .lastName(userInfo.containsKey("lastName") ? userInfo.getString("lastName"):"")
                     .country(userInfo.containsKey("country") ? userInfo.getString("country"):"")
                     .localeName(userInfo.containsKey("locale") ? userInfo.getString("locale"):"")
                     .loginProvider(obj.containsKey("loginProvider") ? obj.getString("loginProvider"):"")
                     .password(Utils.getAlphaNumericString(10))
                     .regAttepmts(0)
                     .build();
        } catch (Exception e) {
            return null;
        }
    }
}
