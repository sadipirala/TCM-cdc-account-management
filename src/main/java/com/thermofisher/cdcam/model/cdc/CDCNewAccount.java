package com.thermofisher.cdcam.model.cdc;

import com.google.gson.Gson;
import com.thermofisher.cdcam.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class CDCNewAccount {
    private String username;
    private String email;
    private String password;
    private String data;
    private String profile;
    //private String preferences;

    public static class CDCNewAccountBuilder {
        public CDCNewAccountBuilder data(Data data) {
            this.data = new Gson().toJson(data);
            return this;
        }

        public CDCNewAccountBuilder profile(Profile profile) throws JSONException {
            this.profile = prepareProfile(profile);
            return this;
        }

        /* public CDCNewAccountBuilder preferences(Preferences preferences) throws JSONException {
            this.preferences = new Gson().toJson(preferences);
            return this;
        } */
    }

    private static String prepareProfile(Profile profile) throws JSONException {
        JSONObject profileJson = Utils.removeNullValuesFromJsonObject(new JSONObject(profile));
        return profileJson.toString();
    }
}
