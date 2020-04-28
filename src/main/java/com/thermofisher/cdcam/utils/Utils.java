package com.thermofisher.cdcam.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monitorjbl.json.JsonViewModule;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Utils {
    public static String getAlphaNumericString(int length) {
        final String ALPHANUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvxyz";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = (int) (ALPHANUMERIC_STRING.length() * Math.random());
            sb.append(ALPHANUMERIC_STRING.charAt(index));
        }
        return sb.toString();
    }

    public static <T> String convertJavaToJsonString(T t) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JsonViewModule());
        return mapper.writeValueAsString(t);
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

    public static String stackTraceToString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    public static JSONObject removeNullValuesFromJsonObject(JSONObject object) throws JSONException {
        JSONArray names = object.names();
        for (int i = 0; i < names.length(); ++i) {
            String key = names.getString(i);
            if (object.isNull(key)) {
                object.remove(key);
            }
        }
        return object;
    }
}
