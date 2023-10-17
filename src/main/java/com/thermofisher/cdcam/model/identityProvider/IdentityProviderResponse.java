package com.thermofisher.cdcam.model.identityProvider;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@Getter
@Setter
@Builder
public class IdentityProviderResponse {
    private String name;
    private String entityID;
    private String singleSignOnServiceUrl;
    private String singleSignOnServiceBinding;
    private String singleLogoutServiceUrl;
    private String singleLogoutServiceBinding;
    private String nameIDFormat;
    private HashMap<String, String> attributeMap;
    private String certificate;
    private String spSigningAlgorithm;
    private Boolean signAuthnRequest;
    private Boolean requireSAMLResponseSigned;
    private Boolean requireAssertionSigned;
    private Boolean requireAssertionEncrypted;
    private Boolean useSessionNotOnOrAfter;
}

