package com.thermofisher.cdcam.utils;

import java.io.FileReader;
import java.io.IOException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.MalformedJsonException;

public class TestUtils {
    public static JsonObject getJSONFromFile (String filePath) throws IOException, JsonSyntaxException {

        //JSONObject jsonObject = new ObjectMapper().readValue(filePath, JSONObject.class);
        JsonObject jsonObject = JsonParser.parseReader(new FileReader(filePath)).getAsJsonObject();
        System.out.println(jsonObject.toString());
        return jsonObject;
    }
}
