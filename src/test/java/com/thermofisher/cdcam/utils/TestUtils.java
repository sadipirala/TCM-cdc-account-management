package com.thermofisher.cdcam.utils;

import java.io.FileReader;
import java.io.IOException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class TestUtils {
    public static JSONObject getJSONFromFile (String filePath) throws IOException, ParseException{
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(filePath));
        return (JSONObject) obj;
    }
}
