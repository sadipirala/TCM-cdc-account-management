package com.thermofisher.cdcam.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monitorjbl.json.JsonViewModule;
import org.json.JSONException;
import org.json.JSONObject;

public class Utils {
    public static String getAlphaNumericString(int lenght) {
        final String ALPHANUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvxyz";
        StringBuilder sb = new StringBuilder(lenght);
        for (int i = 0; i < lenght; i++) {
            int index = (int) (ALPHANUMERIC_STRING.length() * Math.random());
            sb.append(ALPHANUMERIC_STRING.charAt(index));
        }
        return sb.toString();
    }

    public static <T> String convertJavaToJsonString(T t) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JsonViewModule());
        return mapper.writeValueAsString(t);
    }

    public static Object getValueFromJSON(JSONObject object, String value) {
        try {
            return object.get(value);
        } catch (JSONException e) {
            return "";
        }
    }
}
