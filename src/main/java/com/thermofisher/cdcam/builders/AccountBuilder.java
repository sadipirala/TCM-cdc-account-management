package com.thermofisher.cdcam.builders;

import com.gigya.socialize.GSObject;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.utils.Utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AccountBuilder {

    final static Logger logger = LogManager.getLogger("CdcamApp");

    public AccountInfo getAccountInfo(GSObject obj) {
        try {
            String uid = (String) obj.get("UID");
            GSObject data = (GSObject) obj.get("data");
            GSObject profile = (GSObject) obj.get("profile");
            GSObject work = profile.containsKey("work") ? (GSObject) profile.get("work") : null;
            String email = profile.containsKey("email") ? profile.getString("email") : "";
            String company = "";
            String department = "";

            if (work != null) {
                company = work.containsKey("company") ? work.getString("company") : "";
                department = work.containsKey("location") ? work.getString("location") : "";
            } 

            return AccountInfo.builder()
                    .uid(uid)
                    .username(profile.containsKey("username") ? profile.getString("username") : email)
                    .emailAddress(email)
                    .firstName(profile.containsKey("firstName") ? profile.getString("firstName") : "")
                    .lastName(profile.containsKey("lastName") ? profile.getString("lastName") : "")
                    .company(company)
                    .country(profile.containsKey("country") ? profile.getString("country") : "")
                    .city(profile.containsKey("city") ? profile.getString("city") : "")
                    .department(department)
                    .member(data.containsKey("subscribe") ? data.getString("subscribe") : "false")
                    .localeName(profile.containsKey("locale") ? profile.getString("locale") : "")
                    .loginProvider(obj.containsKey("loginProvider") ? obj.getString("loginProvider") : "")
                    .regAttempts(0)
                    .build();
        } catch (Exception e) {
            logger.fatal("Error building account info object:  " + e.getMessage());
            return null;
        }
    }
}
