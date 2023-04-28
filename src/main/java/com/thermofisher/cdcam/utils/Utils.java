package com.thermofisher.cdcam.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monitorjbl.json.JsonViewModule;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utils {
    private final static String VALID_EMAIL_REGEX = "^(?!.*[.]{2})([a-zA-Z0-9])+([a-zA-Z0-9_.\\-])+@(([a-zA-Z0-9\\-])+\\.)+([a-zA-Z0-9]{2,6})$";
    private final static String CHINA_LOCALE = "zh-cn";
    private final static String TAIWAN_LOCALE = "zh-tw";

    public static boolean isValidEmail(String email) {
        return email.matches(VALID_EMAIL_REGEX);
    }

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

    public static String parseLocale(String locale) {
        final int LANGUAGE_CODE_INDEX = 0;
        String lang = locale.split("_")[LANGUAGE_CODE_INDEX];

        if (isChinaLocale(lang)) {
            return CHINA_LOCALE;
        } else if (isTaiwanLocale(lang)) {
            return TAIWAN_LOCALE;
        }

        return lang;
    }

    private static boolean isChinaLocale(String locale) {
        return locale.equalsIgnoreCase("zh");
    }

    private static boolean isTaiwanLocale(String locale) {
        final String TFCOM_LANG_FOR_TAIWAN = "zt";
        return locale.equalsIgnoreCase(TFCOM_LANG_FOR_TAIWAN);
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

    public static boolean isNullOrEmpty(Collection <?> collection) {
        return Objects.isNull(collection) || collection.size() == 0;
    }

    public static boolean isNullOrEmpty(String string) {
        return Objects.isNull(string) || StringUtils.isEmpty(string);
    }

    public static boolean hasNullOrEmptyValues(Collection<String> collection) {
        return collection.stream().anyMatch(Utils::isNullOrEmpty);
    }

    public static  boolean isAValidEmail(String email) {
        return email.matches(VALID_EMAIL_REGEX);
    }
}
