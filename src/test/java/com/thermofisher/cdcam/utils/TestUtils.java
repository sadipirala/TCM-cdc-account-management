package com.thermofisher.cdcam.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.FileReader;
import java.io.IOException;

public class TestUtils {
    public static JsonObject getJSONFromFile(String filePath) throws IOException, JsonSyntaxException {

        JsonObject jsonObject = JsonParser.parseReader(new FileReader(filePath)).getAsJsonObject();
        return jsonObject;
    }
}
