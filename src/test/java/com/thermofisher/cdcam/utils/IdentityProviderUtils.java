package com.thermofisher.cdcam.utils;

import java.io.IOException;
import java.util.HashMap;

import com.thermofisher.cdcam.model.identityProvider.IdentityProviderResponse;

import org.json.simple.parser.ParseException;

public class IdentityProviderUtils {
    
    public static String getIdentityProviderJsonString() throws IOException, ParseException {
        String path = "src/test/resources/CDCResponses/identity-provider.json";
        return TestUtils.getJSONFromFile(path).toString();
    }

    public static IdentityProviderResponse buildTestResponse() {
        return IdentityProviderResponse.builder()
                .name("")
                .entityID("")
                .singleSignOnServiceUrl("")
                .singleSignOnServiceBinding("")
                .singleLogoutServiceUrl("")
                .singleLogoutServiceBinding("")
                .nameIDFormat("")
                .attributeMap(getTestAttributeMap())
                .certificate("")
                .spSigningAlgorithm("")
                .signAuthnRequest(false)
                .requireSAMLResponseSigned(false)
                .requireAssertionSigned(false)
                .requireAssertionEncrypted(false)
                .useSessionNotOnOrAfter(false)
                .build();
    }

    private static HashMap<String, String> getTestAttributeMap() {
        HashMap<String, String> container = new HashMap<String, String>();
        container.put("", "");
        return container;
    }
}
