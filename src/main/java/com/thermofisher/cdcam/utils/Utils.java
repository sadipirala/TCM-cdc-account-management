package com.thermofisher.cdcam.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monitorjbl.json.JsonViewModule;
import org.json.JSONException;
import org.json.JSONObject;

public class Utils {
    public static String getAlphaNumericString(int n) {
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

    public static <T> String convertJavaToJsonString(T t) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JsonViewModule());
        return mapper.writeValueAsString(t);
    }

    public static JSONObject convertStringToJson(String object) {
        try {
            return new JSONObject(object);
        } catch (JSONException e) {
            return null;
        }
    }

    public static String getStringFromJSON(JSONObject object, String value) {
        try {
            return (String) object.get(value);
        } catch (JSONException e) {
            return "";
        }
    }

    public static JSONObject getObjectFromJSON(JSONObject object, String value) {
        try {
            return (JSONObject) object.get(value);
        } catch (JSONException e) {
            return null;
        }
    }
}
