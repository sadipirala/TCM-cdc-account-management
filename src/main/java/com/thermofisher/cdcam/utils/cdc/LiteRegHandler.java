package com.thermofisher.cdcam.utils.cdc;

import com.gigya.socialize.GSResponse;
import com.thermofisher.cdcam.cdc.CDCAccounts;
import com.thermofisher.cdcam.model.EECUser;
import com.thermofisher.cdcam.model.EmailList;
import com.thermofisher.cdcam.utils.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class LiteRegHandler {

    @Autowired
    CDCAccounts cdcAccounts;

    private List<EECUser> users;

    public List<EECUser> process(EmailList emailList) throws JSONException {
        if (emailList.getEmails().size() == 0) return new ArrayList<>();

        users = new ArrayList<>();

        for (String email: emailList.getEmails()) {
            GSResponse response = cdcAccounts.searchByEmail(email);

            if(response != null) {

                JSONObject jsonResponse = new JSONObject(response.getResponseText());

                if (Integer.parseInt(jsonResponse.get("totalCount").toString()) > 0) {
                    JSONArray array = jsonResponse.getJSONArray("results");

                    for(int i = 0; i < array.length(); i++) {
                        users.add(getEECUser(array.getJSONObject(i), email));
                    }
                } else {
                    users.add(liteRegisterUser(email));
                }
            }
        }

        return users;
    }

    private EECUser liteRegisterUser(String email) {
//        GSResponse response = cdcAccounts.setLiteReg(email);
        return null;
    }

    private EECUser getEECUser(JSONObject user, String email) {
        String uid = Utils.getValueFromJSON(user, "UID").toString();
        String isRegistered = Utils.getValueFromJSON(user, "isRegistered").toString();

        boolean registered = false;

        if (!isRegistered.equals("")) {
            registered = Boolean.parseBoolean(isRegistered);
        }

        return EECUser.builder()
                .uid(uid)
                .email(email)
                .registered(registered)
                .cdcResponseCode(200)
                .cdcResponseMessage("OK")
                .build();
    }
}
