package com.thermofisher.cdcam.utils.cdc;

import com.gigya.socialize.GSResponse;
import com.thermofisher.cdcam.enums.cdc.GigyaCodes;

public class CDCUtils {

    public static boolean isSecondaryDCSupported(String env) {
        return env.toLowerCase().contains("qa4") 
            || env.toLowerCase().contains("qa1")
            || env.toLowerCase().contains("prod");
    }

    public static boolean isErrorResponse(GSResponse response) {
        return response.getErrorCode() != GigyaCodes.SUCCESS.getValue();
    }
}
