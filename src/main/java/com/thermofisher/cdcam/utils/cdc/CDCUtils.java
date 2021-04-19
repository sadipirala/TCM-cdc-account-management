package com.thermofisher.cdcam.utils.cdc;

public class CDCUtils {

    public static boolean isSecondaryDCSupported(String env) {
        return env.toLowerCase().contains("qa4") 
            || env.toLowerCase().contains("qa1")
            || env.toLowerCase().contains("prod");
    }
}
