package com.thermofisher.cdcam.model.cdc;

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

    public static class CDCNewAccountBuilder {
        public CDCNewAccountBuilder data(Data data) throws JSONException {
            this.data = prepareData(data);
            return this;
        }

        public CDCNewAccountBuilder profile(Profile profile) throws JSONException {
            this.profile = prepareProfile(profile);
            return this;
        }
    }

    private static String prepareData(Data data) throws JSONException {
        JSONObject dataJson = Utils.removeNullValuesFromJsonObject(new JSONObject(data));
        return dataJson.toString();
    }

    private static String prepareProfile(Profile profile) throws JSONException {
        JSONObject profileJson = Utils.removeNullValuesFromJsonObject(new JSONObject(profile));
        return profileJson.toString();
    }
}