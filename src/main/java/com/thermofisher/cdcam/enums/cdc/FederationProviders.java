package com.thermofisher.cdcam.enums.cdc;

public enum FederationProviders {
    OIDC("oidc"),
    SAML("saml"),
    SITE("site");

    private String value;

    FederationProviders(String value) { this.value = value; }

    public String getValue() { return this.value; }
}
